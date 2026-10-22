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
		File jar = InjectedClientJar.locate();
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
	public void bothGuardShapesAreHookedAndSpoofed() throws Exception
	{
		byte[] patched = new RandomDatTransformer().transform(null, "StubRandomClient", null, null, buildFakeRandomDatClass());
		assertNotNull(patched);
		assertTrue(RandomDatTransformer.isRandomHookInstalled());
		assertEquals(2, RandomDatTransformer.patchedSiteCount());
		assertEquals("StubRandomClient", RandomDatTransformer.fieldOwner());
		assertEquals("fix", RandomDatTransformer.fieldName());

		Class<?> stub = new StubLoader("StubRandomClient", patched).defined();
		byte[] expected = RandomDatStore.getOrCreate(HwidAgent.currentIdentifier());
		byte[] viaAcmp = (byte[]) stub.getMethod("sendAcmp").invoke(null);
		byte[] viaIfNull = (byte[]) stub.getMethod("sendIfNull").invoke(null);
		assertArrayEquals(expected, viaAcmp);
		assertTrue(viaAcmp == viaIfNull);
		assertTrue(viaAcmp == stub.getDeclaredField("fix").get(null));
	}

	@Test
	public void serverPushedRandomDatIsPassedThrough() throws Exception
	{
		byte[] patched = new RandomDatTransformer().transform(null, "StubRandomClient", null, null, buildFakeRandomDatClass());
		Class<?> stub = new StubLoader("StubRandomClient", patched).defined();
		byte[] pushed = new byte[24];
		java.util.Arrays.fill(pushed, (byte) 7);
		stub.getDeclaredField("fix").set(null, pushed);
		assertTrue(pushed == stub.getMethod("sendAcmp").invoke(null));

		stub.getDeclaredField("fix").set(null, null);
		byte[] injected = (byte[]) stub.getMethod("sendAcmp").invoke(null);
		java.util.Arrays.fill(injected, (byte) 9);
		assertTrue(injected == stub.getMethod("sendIfNull").invoke(null));
		assertEquals(9, injected[0]);
	}

	@Test
	public void spoofDisabledFallsBackToVanillaRead() throws Exception
	{
		byte[] patched = new RandomDatTransformer().transform(null, "StubRandomClient", null, null, buildFakeRandomDatClass());
		Class<?> stub = new StubLoader("StubRandomClient", patched).defined();
		byte[] injected = (byte[]) stub.getMethod("sendAcmp").invoke(null);
		assertNotNull(injected);

		System.setProperty(HwidAgent.TOGGLE_PROPERTY, "false");
		try
		{
			byte[] vanilla = (byte[]) stub.getMethod("sendIfNull").invoke(null);
			assertArrayEquals(new byte[24], vanilla);
			assertNull(stub.getDeclaredField("fix").get(null));
		}
		finally
		{
			System.clearProperty(HwidAgent.TOGGLE_PROPERTY);
		}
	}

	@Test
	public void copyHelperIsNotHooked()
	{
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
		cw.visit(Opcodes.V11, Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER, "StubCopyHelper", null, "java/lang/Object", null);
		cw.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "fix", "[B", null, null).visitEnd();
		MethodVisitor copy = cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "copy", "()V", null, null);
		copy.visitCode();
		copy.visitFieldInsn(Opcodes.GETSTATIC, "StubCopyHelper", "fix", "[B");
		org.objectweb.asm.Label done = new org.objectweb.asm.Label();
		copy.visitJumpInsn(Opcodes.IFNONNULL, done);
		copy.visitIntInsn(Opcodes.BIPUSH, 24);
		copy.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_BYTE);
		copy.visitFieldInsn(Opcodes.PUTSTATIC, "StubCopyHelper", "fix", "[B");
		copy.visitLabel(done);
		copy.visitInsn(Opcodes.RETURN);
		copy.visitMaxs(0, 0);
		copy.visitEnd();
		cw.visitEnd();

		assertNull(new RandomDatTransformer().transform(null, "StubCopyHelper", null, null, cw.toByteArray()));
		assertNull(RandomDatTransformer.fieldOwner());
	}

	@Test
	public void randomDatTransformIsIdempotent()
	{
		byte[] patched = new RandomDatTransformer().transform(null, "StubRandomClient", null, null, buildFakeRandomDatClass());
		assertNotNull(patched);
		assertNull(new RandomDatTransformer().transform(null, "StubRandomClient", null, null, patched));
	}

	@Test
	public void realInjectedClientRandomDatGuardsAreHooked() throws Exception
	{
		File jar = InjectedClientJar.locate();
		assertNotNull("injected-client jar missing from test classpath", jar);

		RandomDatTransformer transformer = new RandomDatTransformer();
		java.util.List<String> patchedClasses = new java.util.ArrayList<>();
		try (ZipFile zip = new ZipFile(jar))
		{
			ZipEntry clientEntry = zip.getEntry("client.class");
			assertNotNull(clientEntry);
			java.util.List<ZipEntry> ordered = new java.util.ArrayList<>();
			ordered.add(clientEntry);
			for (java.util.Enumeration<? extends ZipEntry> entries = zip.entries(); entries.hasMoreElements();)
			{
				ZipEntry entry = entries.nextElement();
				if (!entry.isDirectory() && entry.getName().endsWith(".class") && !entry.equals(clientEntry))
				{
					ordered.add(entry);
				}
			}
			for (ZipEntry entry : ordered)
			{
				byte[] bytes;
				try (InputStream in = zip.getInputStream(entry))
				{
					bytes = in.readAllBytes();
				}
				byte[] result = transformer.transform(null, zipNameToClassName(entry.getName()), null, null, bytes);
				if (result != null)
				{
					patchedClasses.add(entry.getName());
					assertEquals(0, unhookedGuards(result, RandomDatTransformer.fieldOwner(), RandomDatTransformer.fieldName()));
				}
			}
		}
		assertEquals("client", RandomDatTransformer.fieldOwner());
		assertTrue(patchedClasses.contains("client.class"));
		assertTrue("expected the login send sites to be hooked, got " + RandomDatTransformer.patchedSiteCount(),
			RandomDatTransformer.patchedSiteCount() >= 2);
	}

	private static int unhookedGuards(byte[] classBytes, String owner, String name)
	{
		org.objectweb.asm.tree.ClassNode node = new org.objectweb.asm.tree.ClassNode();
		new org.objectweb.asm.ClassReader(classBytes).accept(node, 0);
		int unhooked = 0;
		for (MethodNode method : node.methods)
		{
			for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext())
			{
				if (insn.getOpcode() != Opcodes.GETSTATIC
					|| !owner.equals(((org.objectweb.asm.tree.FieldInsnNode) insn).owner)
					|| !name.equals(((org.objectweb.asm.tree.FieldInsnNode) insn).name))
				{
					continue;
				}
				AbstractInsnNode next = insn.getNext();
				boolean guard = next != null && (next.getOpcode() == Opcodes.IFNULL || next.getOpcode() == Opcodes.IF_ACMPEQ);
				AbstractInsnNode previous = insn.getPrevious();
				if (previous != null && previous.getOpcode() == Opcodes.ACONST_NULL)
				{
					previous = previous.getPrevious();
				}
				boolean hooked = previous != null && previous.getOpcode() == Opcodes.PUTSTATIC;
				if (guard && !hooked && storesFreshArray(method))
				{
					unhooked++;
				}
			}
		}
		return unhooked;
	}

	private static boolean storesFreshArray(MethodNode method)
	{
		for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext())
		{
			if (insn.getOpcode() == Opcodes.NEWARRAY && insn.getNext() != null && insn.getNext().getOpcode() == Opcodes.ASTORE)
			{
				return true;
			}
		}
		return false;
	}

	private static final class StubLoader extends ClassLoader
	{
		private final String name;

		StubLoader(String name, byte[] bytes)
		{
			super(StubLoader.class.getClassLoader());
			this.name = name;
			defineClass(name, bytes, 0, bytes.length);
		}

		Class<?> defined() throws ClassNotFoundException
		{
			return loadClass(name);
		}
	}

	private static byte[] buildFakeRandomDatClass()
	{
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
		cw.visit(Opcodes.V11, Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER, "StubRandomClient", null, "java/lang/Object", null);
		cw.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "fix", "[B", null, null).visitEnd();

		MethodVisitor acmp = cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "sendAcmp", "()[B", null, null);
		acmp.visitCode();
		acmp.visitInsn(Opcodes.ACONST_NULL);
		acmp.visitFieldInsn(Opcodes.GETSTATIC, "StubRandomClient", "fix", "[B");
		org.objectweb.asm.Label acmpFresh = new org.objectweb.asm.Label();
		acmp.visitJumpInsn(Opcodes.IF_ACMPEQ, acmpFresh);
		acmp.visitFieldInsn(Opcodes.GETSTATIC, "StubRandomClient", "fix", "[B");
		acmp.visitInsn(Opcodes.ARETURN);
		acmp.visitLabel(acmpFresh);
		acmp.visitIntInsn(Opcodes.BIPUSH, 24);
		acmp.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_BYTE);
		acmp.visitVarInsn(Opcodes.ASTORE, 0);
		acmp.visitVarInsn(Opcodes.ALOAD, 0);
		acmp.visitInsn(Opcodes.ARETURN);
		acmp.visitMaxs(0, 0);
		acmp.visitEnd();

		MethodVisitor ifNull = cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "sendIfNull", "()[B", null, null);
		ifNull.visitCode();
		ifNull.visitFieldInsn(Opcodes.GETSTATIC, "StubRandomClient", "fix", "[B");
		org.objectweb.asm.Label ifNullFresh = new org.objectweb.asm.Label();
		ifNull.visitJumpInsn(Opcodes.IFNULL, ifNullFresh);
		ifNull.visitFieldInsn(Opcodes.GETSTATIC, "StubRandomClient", "fix", "[B");
		ifNull.visitInsn(Opcodes.ARETURN);
		ifNull.visitLabel(ifNullFresh);
		ifNull.visitIntInsn(Opcodes.BIPUSH, 24);
		ifNull.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_BYTE);
		ifNull.visitVarInsn(Opcodes.ASTORE, 0);
		ifNull.visitVarInsn(Opcodes.ALOAD, 0);
		ifNull.visitInsn(Opcodes.ARETURN);
		ifNull.visitMaxs(0, 0);
		ifNull.visitEnd();

		cw.visitEnd();
		return cw.toByteArray();
	}
}
