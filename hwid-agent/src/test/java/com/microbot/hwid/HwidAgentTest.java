package com.microbot.hwid;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class HwidAgentTest
{
	private static final String MARKER_COMMAND = "wmic csproduct get UUID";
	private static final String MARKER_OTHER = "/etc/machine-id";
	private static final String FALLBACK_DEVICE_ID = "12345678-0000-0000-0000-123456789012";

	private File storeFile;
	private File randomDatStoreFile;

	@Before
	public void setUp() throws Exception
	{
		storeFile = File.createTempFile("hwid-test", ".properties");
		DeviceIdStore.setStoreFileForTesting(storeFile);
		DeviceIdStore.reset();
		System.setProperty("microbot.hwid.store", storeFile.getAbsolutePath());
		randomDatStoreFile = File.createTempFile("hwid-rd-test", ".properties");
		RandomDatStore.setStoreFileForTesting(randomDatStoreFile);
		RandomDatStore.reset();
		System.setProperty("microbot.hwid.randomdat.store", randomDatStoreFile.getAbsolutePath());
	}

	@After
	public void tearDown()
	{
		System.clearProperty("microbot.hwid.store");
		System.clearProperty("microbot.hwid.randomdat.store");
		DeviceIdStore.reset();
		RandomDatStore.reset();
		RandomDatTransformer.resetForTesting();
		storeFile.delete();
		randomDatStoreFile.delete();
	}

	private static byte[] buildFakePlatformClass()
	{
		ClassWriter cw = new ClassWriter(0);
		cw.visit(Opcodes.V11, Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER, "FakePlatform", null, "java/lang/Object", null);

		MethodVisitor ctor = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
		ctor.visitCode();
		ctor.visitVarInsn(Opcodes.ALOAD, 0);
		ctor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		ctor.visitInsn(Opcodes.RETURN);
		ctor.visitMaxs(1, 1);
		ctor.visitEnd();

		MethodVisitor ac = cw.visitMethod(Opcodes.ACC_PUBLIC, "ac", "(I)Ljava/lang/String;", null, null);
		ac.visitCode();
		ac.visitLdcInsn(MARKER_COMMAND);
		ac.visitLdcInsn(MARKER_OTHER);
		ac.visitInsn(Opcodes.POP);
		ac.visitInsn(Opcodes.ARETURN);
		ac.visitMaxs(2, 2);
		ac.visitEnd();

		MethodVisitor re = cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "re", "(LFakePlatform;I)Ljava/lang/String;", null, null);
		re.visitCode();
		re.visitLdcInsn(MARKER_OTHER);
		re.visitInsn(Opcodes.ARETURN);
		re.visitMaxs(1, 2);
		re.visitEnd();

		cw.visitEnd();
		return cw.toByteArray();
	}

	private static final class PatchedClassLoader extends ClassLoader
	{
		PatchedClassLoader(byte[] bytes)
		{
			super(PatchedClassLoader.class.getClassLoader());
			defineClass("FakePlatform", bytes, 0, bytes.length);
		}

		Class<?> defined()
		{
			try
			{
				return loadClass("FakePlatform");
			}
			catch (ClassNotFoundException e)
			{
				throw new IllegalStateException(e);
			}
		}
	}

	@Test
	public void patchedMethodsReturnSpoofedUuids() throws Exception
	{
		PlatformInfoTransformer transformer = new PlatformInfoTransformer();
		byte[] patched = transformer.transform(null, "FakePlatform", null, null, buildFakePlatformClass());
		assertNotNull(patched);

		Class<?> fake = new PatchedClassLoader(patched).defined();
		Object instance = fake.getDeclaredConstructor().newInstance();

		Method ac = fake.getMethod("ac", int.class);
		String spoofed = (String) ac.invoke(instance, 1);
		assertNotNull(UUID.fromString(spoofed));
		assertEquals(spoofed, ac.invoke(instance, 2));

		Method re = fake.getMethod("re", fake, int.class);
		assertNotNull(UUID.fromString((String) re.invoke(null, instance, 2)));

		assertEquals(spoofed, HwidAgent.deviceId(3));
	}

	@Test
	public void transformerSkipsUnrelatedClasses() throws Exception
	{
		PlatformInfoTransformer transformer = new PlatformInfoTransformer();
		try (InputStream in = PlatformInfoTransformer.class.getResourceAsStream("DeviceIdStore.class"))
		{
			assertNotNull(in);
			assertNull(transformer.transform(null, "com/microbot/hwid/DeviceIdStore", null, null, in.readAllBytes()));
		}
	}

	@Test
	public void realInjectedClientClassIsPatched() throws Exception
	{
		File jar = locateInjectedClientJar();
		assertNotNull("injected-client jar missing from test classpath", jar);

		PlatformInfoTransformer transformer = new PlatformInfoTransformer();
		int patchedCount = 0;
		try (ZipFile zip = new ZipFile(jar))
		{
			for (java.util.Enumeration<? extends ZipEntry> entries = zip.entries(); entries.hasMoreElements();)
			{
				ZipEntry entry = entries.nextElement();
				if (entry.isDirectory() || !entry.getName().endsWith(".class"))
				{
					continue;
				}
				byte[] bytes;
				try (InputStream in = zip.getInputStream(entry))
				{
					bytes = in.readAllBytes();
				}
				byte[] result = transformer.transform(null, zipNameToClassName(entry.getName()), null, null, bytes);
				if (result != null)
				{
					patchedCount++;
					assertTrue("class " + entry.getName() + " still contains fingerprint commands",
						!stillReferencesFingerprintCommands(result));
				}
			}
		}
		assertEquals(1, patchedCount);
	}

	private static String zipNameToClassName(String entryName)
	{
		return entryName.substring(0, entryName.length() - ".class".length());
	}

	private static boolean stillReferencesFingerprintCommands(byte[] classBytes) throws Exception
	{
		org.objectweb.asm.ClassReader reader = new org.objectweb.asm.ClassReader(classBytes);
		org.objectweb.asm.tree.ClassNode node = new org.objectweb.asm.tree.ClassNode();
		reader.accept(node, 0);
		for (MethodNode method : node.methods)
		{
			for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext())
			{
				if (insn instanceof LdcInsnNode && ((LdcInsnNode) insn).cst instanceof String)
				{
					String value = (String) ((LdcInsnNode) insn).cst;
					if (value.contains("csproduct") || value.contains("/etc/machine-id"))
					{
						return true;
					}
				}
			}
		}
		return false;
	}

	private static File locateInjectedClientJar()
	{
		for (String entry : System.getProperty("java.class.path", "").split(File.pathSeparator))
		{
			if (entry.endsWith(".jar") && entry.contains("injected-client"))
			{
				return new File(entry);
			}
		}
		ClassLoader loader = HwidAgentTest.class.getClassLoader();
		while (loader != null)
		{
			if (loader instanceof java.net.URLClassLoader)
			{
				for (java.net.URL url : ((java.net.URLClassLoader) loader).getURLs())
				{
					String path = url.getPath();
					if (path.endsWith(".jar") && path.contains("injected-client"))
					{
						return new File(java.net.URLDecoder.decode(path, java.nio.charset.StandardCharsets.UTF_8));
					}
				}
			}
			loader = loader.getParent();
		}
		return null;
	}

	@Test
	public void spoofCanBeDisabled()
	{
		System.setProperty(HwidAgent.TOGGLE_PROPERTY, "false");
		try
		{
			assertEquals(FALLBACK_DEVICE_ID, HwidAgent.deviceId(2));
			assertEquals(VanillaDeviceId.getDeviceId(2), HwidAgent.deviceId(2));
		}
		finally
		{
			System.clearProperty(HwidAgent.TOGGLE_PROPERTY);
		}
	}

	@Test
	public void identifierFallsBackToDefaultWhenClientAbsent()
	{
		assertTrue(HwidAgent.spoofEnabled());
		assertEquals("default", HwidAgent.currentIdentifier());
	}

	@Test
	public void randomDatSitesAreSwappedAndSpoofedPerIdentifier() throws Exception
	{
		byte[] candidate = buildFakeRandomDatClass();
		RandomDatTransformer transformer = new RandomDatTransformer();
		byte[] patched = transformer.transform(null, "StubRandomClient", null, null, candidate);
		assertNotNull(patched);
		assertTrue(RandomDatTransformer.isRandomHookInstalled());
		assertEquals(2, RandomDatTransformer.patchedSiteCount());
		assertEquals("com/microbot/hwid/StubOrig", RandomDatTransformer.providerOwner());
		assertEquals("ab", RandomDatTransformer.providerName());

		byte[] first = HwidAgent.randomData((byte) 0);
		assertEquals(24, first.length);
		assertArrayEquals(first, HwidAgent.randomData((byte) 1));
	}

	@Test
	public void randomDatPassthroughWhenSpoofDisabled() throws Exception
	{
		defineStubOrig();
		System.setProperty(HwidAgent.TOGGLE_PROPERTY, "false");
		try
		{
			byte[] candidate = buildFakeRandomDatClass();
			RandomDatTransformer transformer = new RandomDatTransformer();
			byte[] patched = transformer.transform(null, "StubRandomClient", null, null, candidate);
			assertNotNull(patched);
			assertTrue(RandomDatTransformer.isRandomHookInstalled());
			byte[] result = HwidAgent.randomData((byte) 7);
			assertArrayEquals(new byte[] {1, 2, 3, 4}, result);
		}
		finally
		{
			System.clearProperty(HwidAgent.TOGGLE_PROPERTY);
		}
	}

	@Test
	public void randomDatTransformIsIdempotent()
	{
		byte[] patched = new RandomDatTransformer().transform(null, "StubRandomClient", null, null, buildFakeRandomDatClass());
		assertNotNull(patched);
		assertNull(new RandomDatTransformer().transform(null, "StubRandomClient", null, null, patched));
	}

	@Test
	public void realInjectedClientRandomDatProviderIsSwapped() throws Exception
	{
		File jar = locateInjectedClientJar();
		assertNotNull("injected-client jar missing from test classpath", jar);

		RandomDatTransformer transformer = new RandomDatTransformer();
		int patchedCount = 0;
		try (ZipFile zip = new ZipFile(jar))
		{
			for (java.util.Enumeration<? extends ZipEntry> entries = zip.entries(); entries.hasMoreElements();)
			{
				ZipEntry entry = entries.nextElement();
				if (entry.isDirectory() || !entry.getName().endsWith(".class"))
				{
					continue;
				}
				byte[] bytes;
				try (InputStream in = zip.getInputStream(entry))
				{
					bytes = in.readAllBytes();
				}
				byte[] result = transformer.transform(null, zipNameToClassName(entry.getName()), null, null, bytes);
				if (result != null)
				{
					patchedCount++;
					assertTrue(!stillReferencesRandomDatProvider(result, RandomDatTransformer.providerOwner(), RandomDatTransformer.providerName()));
				}
			}
		}
		assertEquals(1, patchedCount);
		assertEquals("jl", RandomDatTransformer.providerOwner());
		assertEquals("ab", RandomDatTransformer.providerName());
		assertEquals(3, RandomDatTransformer.patchedSiteCount());
	}

	private static boolean stillReferencesRandomDatProvider(byte[] classBytes, String owner, String name)
	{
		org.objectweb.asm.ClassReader reader = new org.objectweb.asm.ClassReader(classBytes);
		org.objectweb.asm.tree.ClassNode node = new org.objectweb.asm.tree.ClassNode();
		reader.accept(node, 0);
		for (MethodNode method : node.methods)
		{
			for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext())
			{
				if (insn instanceof org.objectweb.asm.tree.MethodInsnNode
					&& insn.getOpcode() == Opcodes.INVOKESTATIC
					&& owner.equals(((org.objectweb.asm.tree.MethodInsnNode) insn).owner)
					&& name.equals(((org.objectweb.asm.tree.MethodInsnNode) insn).name))
				{
					return true;
				}
			}
		}
		return false;
	}

	private static void defineStubOrig()
	{
		if (stubOrigDefined)
		{
			return;
		}
		ClassWriter cw = new ClassWriter(0);
		cw.visit(Opcodes.V11, Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER, "com/microbot/hwid/StubOrig", null, "java/lang/Object", null);
		MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "ab", "(B)[B", null, null);
		mv.visitCode();
		mv.visitInsn(Opcodes.ICONST_4);
		mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_BYTE);
		mv.visitInsn(Opcodes.DUP);
		mv.visitInsn(Opcodes.ICONST_0);
		mv.visitInsn(Opcodes.ICONST_1);
		mv.visitInsn(Opcodes.BASTORE);
		mv.visitInsn(Opcodes.DUP);
		mv.visitInsn(Opcodes.ICONST_1);
		mv.visitInsn(Opcodes.ICONST_2);
		mv.visitInsn(Opcodes.BASTORE);
		mv.visitInsn(Opcodes.DUP);
		mv.visitInsn(Opcodes.ICONST_2);
		mv.visitInsn(Opcodes.ICONST_3);
		mv.visitInsn(Opcodes.BASTORE);
		mv.visitInsn(Opcodes.DUP);
		mv.visitInsn(Opcodes.ICONST_3);
		mv.visitInsn(Opcodes.ICONST_4);
		mv.visitInsn(Opcodes.BASTORE);
		mv.visitInsn(Opcodes.ARETURN);
		mv.visitMaxs(4, 2);
		mv.visitEnd();
		cw.visitEnd();
		try
		{
			java.lang.invoke.MethodHandles.lookup().defineClass(cw.toByteArray());
		}
		catch (IllegalAccessException e)
		{
			throw new IllegalStateException(e);
		}
		stubOrigDefined = true;
	}

	private static boolean stubOrigDefined;

	private static byte[] buildFakeRandomDatClass()
	{
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
		cw.visit(Opcodes.V11, Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER, "StubRandomClient", null, "java/lang/Object", null);
		cw.visitField(Opcodes.ACC_STATIC, "fix", "[B", null, null).visitEnd();

		MethodVisitor mk = cw.visitMethod(Opcodes.ACC_STATIC, "mk", "()V", null, null);
		mk.visitCode();
		mk.visitIntInsn(Opcodes.BIPUSH, 24);
		mk.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_BYTE);
		mk.visitFieldInsn(Opcodes.PUTSTATIC, "StubRandomClient", "fix", "[B");
		mk.visitInsn(Opcodes.RETURN);
		mk.visitMaxs(1, 0);
		mk.visitEnd();

		MethodVisitor send = cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "send", "()V", null, null);
		send.visitCode();
		send.visitFieldInsn(Opcodes.GETSTATIC, "StubRandomClient", "fix", "[B");
		org.objectweb.asm.Label fresh = new org.objectweb.asm.Label();
		send.visitJumpInsn(Opcodes.IFNULL, fresh);
		send.visitInsn(Opcodes.ICONST_0);
		send.visitMethodInsn(Opcodes.INVOKESTATIC, "com/microbot/hwid/StubOrig", "ab", "(B)[B", false);
		send.visitInsn(Opcodes.POP);
		send.visitInsn(Opcodes.RETURN);
		send.visitLabel(fresh);
		send.visitInsn(Opcodes.ICONST_0);
		send.visitMethodInsn(Opcodes.INVOKESTATIC, "com/microbot/hwid/StubOrig", "ab", "(B)[B", false);
		send.visitInsn(Opcodes.POP);
		send.visitInsn(Opcodes.RETURN);
		send.visitMaxs(2, 0);
		send.visitEnd();

		cw.visitEnd();
		return cw.toByteArray();
	}
}
