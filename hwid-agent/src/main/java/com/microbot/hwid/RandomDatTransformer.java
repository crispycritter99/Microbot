package com.microbot.hwid;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.logging.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public final class RandomDatTransformer implements ClassFileTransformer
{
	private static final Logger log = Logger.getLogger(RandomDatTransformer.class.getName());

	private static final String HOOK_CLASS_INTERNAL = "com/microbot/hwid/HwidAgent";
	private static final String HOOK_METHOD = "randomData";
	private static final String BYTE_ARRAY_DESCRIPTOR = "[B";
	private static final String PROVIDER_DESCRIPTOR = "(B)[B";

	private static volatile boolean randomHookInstalled;
	private static volatile int patchedSites;
	private static volatile String providerOwner;
	private static volatile String providerName;
	private static final Object providerLock = new Object();

	public byte[] transform(ClassLoader loader, String className, Class<?> beingLoaded, ProtectionDomain pd, byte[] bytes)
	{
		try
		{
			return transformInternal(className, bytes);
		}
		catch (Throwable t)
		{
			log.warning("hwid-agent random.dat transform skipped on " + className + ": " + t);
			return null;
		}
	}

	private byte[] transformInternal(String className, byte[] bytes) throws Exception
	{
		if (className == null || looksUnlikely(className))
		{
			return null;
		}

		ClassReader reader = new ClassReader(bytes);
		ClassNode node = new ClassNode();
		reader.accept(node, 0);

		if (alreadyPatched(node))
		{
			return null;
		}

		String fieldName = soleStaticByteArrayField(node);
		if (fieldName == null || !containsNewArray24(node))
		{
			return null;
		}

		String owner = null;
		String name = null;
		int sites = 0;
		for (MethodNode method : node.methods)
		{
			if (!qualifies(method, fieldName))
			{
				continue;
			}
			for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext())
			{
				if (!(insn instanceof MethodInsnNode) || insn.getOpcode() != Opcodes.INVOKESTATIC)
				{
					continue;
				}
				MethodInsnNode call = (MethodInsnNode) insn;
				if (!PROVIDER_DESCRIPTOR.equals(call.desc)
					|| call.owner.equals(node.name)
					|| HOOK_CLASS_INTERNAL.equals(call.owner))
				{
					continue;
				}
				if (owner != null && !owner.equals(call.owner) || name != null && !name.equals(call.name))
				{
					return null;
				}
				owner = call.owner;
				name = call.name;
				sites++;
			}
		}

		if (owner == null || sites == 0)
		{
			return null;
		}

		for (MethodNode method : node.methods)
		{
			for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext())
			{
				if (!(insn instanceof MethodInsnNode) || insn.getOpcode() != Opcodes.INVOKESTATIC)
				{
					continue;
				}
				MethodInsnNode call = (MethodInsnNode) insn;
				if (!PROVIDER_DESCRIPTOR.equals(call.desc) || !owner.equals(call.owner) || !name.equals(call.name))
				{
					continue;
				}
				method.instructions.set(insn, new MethodInsnNode(
					Opcodes.INVOKESTATIC, HOOK_CLASS_INTERNAL, HOOK_METHOD, PROVIDER_DESCRIPTOR, false));
			}
		}

		synchronized (providerLock)
		{
			if (providerOwner == null)
			{
				providerOwner = owner;
				providerName = name;
				patchedSites = sites;
				randomHookInstalled = true;
				log.info("hwid-agent random.dat spoof installed on " + node.name + " (" + sites + " sites)");
			}
		}

		ClassWriter writer = new ClassWriter(0);
		node.accept(writer);
		return writer.toByteArray();
	}

	private static boolean looksUnlikely(String className)
	{
		return className.startsWith("java/")
			|| className.startsWith("javax/")
			|| className.startsWith("sun/")
			|| className.startsWith("com/sun/")
			|| className.startsWith("jdk/")
			|| className.startsWith("org/")
			|| className.startsWith("io/")
			|| className.startsWith("okhttp")
			|| className.startsWith("okio")
			|| className.startsWith("kotlin")
			|| className.startsWith("net/")
			|| className.startsWith("com/")
			|| className.startsWith("au/")
			|| className.startsWith("[");
	}

	private static boolean alreadyPatched(ClassNode node)
	{
		for (MethodNode method : node.methods)
		{
			for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext())
			{
				if (insn instanceof MethodInsnNode
					&& insn.getOpcode() == Opcodes.INVOKESTATIC
					&& HOOK_METHOD.equals(((MethodInsnNode) insn).name)
					&& HOOK_CLASS_INTERNAL.equals(((MethodInsnNode) insn).owner))
				{
					return true;
				}
			}
		}
		return false;
	}

	private static String soleStaticByteArrayField(ClassNode node)
	{
		String fieldName = null;
		int count = 0;
		for (FieldNode field : node.fields)
		{
			if ((field.access & Opcodes.ACC_STATIC) != 0 && BYTE_ARRAY_DESCRIPTOR.equals(field.desc))
			{
				count++;
				fieldName = field.name;
			}
		}
		return count == 1 ? fieldName : null;
	}

	private static boolean containsNewArray24(ClassNode node)
	{
		for (MethodNode method : node.methods)
		{
			for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext())
			{
				if (!(insn instanceof IntInsnNode)
					|| insn.getOpcode() != Opcodes.BIPUSH
					|| ((IntInsnNode) insn).operand != 24)
				{
					continue;
				}
				AbstractInsnNode next = insn.getNext();
				if (next != null
					&& next.getOpcode() == Opcodes.NEWARRAY
					&& ((IntInsnNode) next).operand == Opcodes.T_BYTE)
				{
					return true;
				}
			}
		}
		return false;
	}

	private static boolean qualifies(MethodNode method, String fieldName)
	{
		boolean hasNullCheck = false;
		boolean hasProvider = false;
		for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext())
		{
			if (!hasNullCheck
				&& insn instanceof FieldInsnNode
				&& insn.getOpcode() == Opcodes.GETSTATIC
				&& BYTE_ARRAY_DESCRIPTOR.equals(((FieldInsnNode) insn).desc)
				&& ((FieldInsnNode) insn).name.equals(fieldName))
			{
				AbstractInsnNode next = insn.getNext();
				if (next != null && next.getOpcode() == Opcodes.IFNULL)
				{
					hasNullCheck = true;
				}
			}
			if (!hasProvider
				&& insn instanceof MethodInsnNode
				&& insn.getOpcode() == Opcodes.INVOKESTATIC
				&& PROVIDER_DESCRIPTOR.equals(((MethodInsnNode) insn).desc))
			{
				hasProvider = true;
			}
		}
		return hasNullCheck && hasProvider;
	}

	static boolean isRandomHookInstalled()
	{
		return randomHookInstalled;
	}

	static int patchedSiteCount()
	{
		return patchedSites;
	}

	static String providerOwner()
	{
		return providerOwner;
	}

	static String providerName()
	{
		return providerName;
	}

	static void resetForTesting()
	{
		synchronized (providerLock)
		{
			randomHookInstalled = false;
			patchedSites = 0;
			providerOwner = null;
			providerName = null;
		}
	}
}
