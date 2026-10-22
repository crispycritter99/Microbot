package com.microbot.hwid;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public final class RandomDatTransformer implements ClassFileTransformer
{
	private static final Logger log = Logger.getLogger(RandomDatTransformer.class.getName());

	private static final String HOOK_CLASS_INTERNAL = "com/microbot/hwid/HwidAgent";
	private static final String HOOK_METHOD = "randomDatField";
	private static final String HOOK_DESCRIPTOR = "([B)[B";
	private static final String BYTE_ARRAY_DESCRIPTOR = "[B";

	private static final Object lock = new Object();
	private static volatile boolean randomHookInstalled;
	private static volatile int patchedSites;
	private static volatile String fieldOwner;
	private static volatile String fieldName;

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

	private byte[] transformInternal(String className, byte[] bytes)
	{
		if (className == null || bytes == null || looksUnlikely(className))
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

		String owner = fieldOwner;
		String name = fieldName;
		if (owner == null)
		{
			name = soleStaticByteArrayField(node);
			if (name == null)
			{
				return null;
			}
			owner = node.name;
		}

		List<FieldInsnNode> guards = new ArrayList<>();
		for (MethodNode method : node.methods)
		{
			if (!storesFreshArrayInLocal(method))
			{
				continue;
			}
			for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext())
			{
				if (isNullGuard(insn, owner, name))
				{
					guards.add((FieldInsnNode) insn);
				}
			}
		}

		if (guards.isEmpty())
		{
			return null;
		}

		synchronized (lock)
		{
			if (fieldOwner == null)
			{
				fieldOwner = owner;
				fieldName = name;
			}
			else if (!fieldOwner.equals(owner) || !fieldName.equals(name))
			{
				return null;
			}
			patchedSites += guards.size();
			randomHookInstalled = true;
		}

		for (FieldInsnNode guard : guards)
		{
			InsnList hook = new InsnList();
			hook.add(new FieldInsnNode(Opcodes.GETSTATIC, owner, name, BYTE_ARRAY_DESCRIPTOR));
			hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOK_CLASS_INTERNAL, HOOK_METHOD, HOOK_DESCRIPTOR, false));
			hook.add(new FieldInsnNode(Opcodes.PUTSTATIC, owner, name, BYTE_ARRAY_DESCRIPTOR));
			findMethod(node, guard).instructions.insertBefore(guard, hook);
		}

		log.info("hwid-agent random.dat spoof installed on " + node.name + " (" + guards.size() + " sites, field " + owner + "." + name + ")");

		ClassWriter writer = new ClassWriter(0);
		node.accept(writer);
		return writer.toByteArray();
	}

	private static MethodNode findMethod(ClassNode node, AbstractInsnNode insn)
	{
		for (MethodNode method : node.methods)
		{
			if (method.instructions.contains(insn))
			{
				return method;
			}
		}
		throw new IllegalStateException("guard instruction without method");
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
		String name = null;
		int count = 0;
		for (FieldNode field : node.fields)
		{
			if ((field.access & Opcodes.ACC_STATIC) != 0 && BYTE_ARRAY_DESCRIPTOR.equals(field.desc))
			{
				count++;
				name = field.name;
			}
		}
		return count == 1 ? name : null;
	}

	private static boolean storesFreshArrayInLocal(MethodNode method)
	{
		for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext())
		{
			if (insn.getOpcode() != Opcodes.BIPUSH || ((IntInsnNode) insn).operand != 24)
			{
				continue;
			}
			AbstractInsnNode array = nextReal(insn);
			if (array == null
				|| array.getOpcode() != Opcodes.NEWARRAY
				|| ((IntInsnNode) array).operand != Opcodes.T_BYTE)
			{
				continue;
			}
			AbstractInsnNode store = nextReal(array);
			if (store != null && store.getOpcode() == Opcodes.ASTORE)
			{
				return true;
			}
		}
		return false;
	}

	private static boolean isNullGuard(AbstractInsnNode insn, String owner, String name)
	{
		if (insn.getOpcode() != Opcodes.GETSTATIC)
		{
			return false;
		}
		FieldInsnNode field = (FieldInsnNode) insn;
		if (!owner.equals(field.owner) || !name.equals(field.name) || !BYTE_ARRAY_DESCRIPTOR.equals(field.desc))
		{
			return false;
		}

		AbstractInsnNode next = nextReal(insn);
		if (next == null)
		{
			return false;
		}
		int op = next.getOpcode();
		if (op == Opcodes.IFNULL || op == Opcodes.IFNONNULL)
		{
			return true;
		}
		if (isReferenceCompare(op))
		{
			AbstractInsnNode previous = previousReal(insn);
			return previous != null && previous.getOpcode() == Opcodes.ACONST_NULL;
		}
		if (op == Opcodes.ACONST_NULL)
		{
			AbstractInsnNode compare = nextReal(next);
			return compare != null && isReferenceCompare(compare.getOpcode());
		}
		return false;
	}

	private static boolean isReferenceCompare(int opcode)
	{
		return opcode == Opcodes.IF_ACMPEQ || opcode == Opcodes.IF_ACMPNE;
	}

	private static AbstractInsnNode nextReal(AbstractInsnNode insn)
	{
		AbstractInsnNode next = insn.getNext();
		while (next != null && next.getOpcode() < 0)
		{
			next = next.getNext();
		}
		return next;
	}

	private static AbstractInsnNode previousReal(AbstractInsnNode insn)
	{
		AbstractInsnNode previous = insn.getPrevious();
		while (previous != null && previous.getOpcode() < 0)
		{
			previous = previous.getPrevious();
		}
		return previous;
	}

	static boolean isRandomHookInstalled()
	{
		return randomHookInstalled;
	}

	static int patchedSiteCount()
	{
		return patchedSites;
	}

	static String fieldOwner()
	{
		return fieldOwner;
	}

	static String fieldName()
	{
		return fieldName;
	}

	static void resetForTesting()
	{
		synchronized (lock)
		{
			randomHookInstalled = false;
			patchedSites = 0;
			fieldOwner = null;
			fieldName = null;
		}
	}
}
