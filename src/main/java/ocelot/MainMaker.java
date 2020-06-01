package ocelot;

import static java.util.stream.Collectors.toList;
import static org.objectweb.asm.Opcodes.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.objectweb.asm.ClassWriter;

public class MainMaker {
  private final static Map<String, Byte> opcodeLookup = new HashMap<>();

  private ClassWriter cw = new ClassWriter(0);

  public static String normalizeLine(String l) {
    var realLine = l;
    var parts = l.split(": ");
    if (parts.length > 1) {
      realLine = parts[1];
    }
    return realLine.toUpperCase();
  }

  static void init() {
    try (var is = MainMaker.class.getClassLoader().getResourceAsStream("opcodes.txt");) {
      var all = new String(is.readAllBytes());
      for (var l : all.split("\n")) {
        var symVal = l.split(",");
        var val = Integer.parseInt(symVal[1]);
        opcodeLookup.put(symVal[0], (byte) val);
      }
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(255);
    }
  }

  public static void main(String[] args) {
    if (args.length < 1) {
      System.err.println("Usage: MainMaker <files>");
      System.exit(1);
    }

    init();
    MainMaker m = new MainMaker();
    try {
      for (var fName : args) {
        var asm = Files.readAllLines(Path.of(fName)).stream().map(l -> normalizeLine(l)).collect(toList());
        Files.write(Paths.get(fName + ".class"), m.serializeToBytes(fName, asm.toArray(new String[0])));
      }
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(2);
    }
  }

  private byte[] serializeToBytes(String outputClazzName, String[] opcodes) {
    cw.visit(V1_1, ACC_PUBLIC + ACC_SUPER, outputClazzName, null, "java/lang/Object", null);
    addStandardConstructor();
    addMethod(opcodes);
    cw.visitEnd();
    return cw.toByteArray();
  }

  private void addMethod(String[] opcodes) {
    var mv =
            cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "a", "()I", null, null);
    mv.visitCode();

    for (int i = 0; i < opcodes.length; i = i + 1) {
      var current = opcodeLookup.get(opcodes[i]);
      switch (current) {
        case NOP, ACONST_NULL, ICONST_M1, ICONST_0, ICONST_1, ICONST_2, ICONST_3, ICONST_4, ICONST_5,
        LCONST_0, LCONST_1, FCONST_0, FCONST_1, FCONST_2, DCONST_0, DCONST_1, IALOAD, LALOAD,
        FALOAD, DALOAD, AALOAD, BALOAD, CALOAD, SALOAD, IASTORE, LASTORE, FASTORE, DASTORE,
        AASTORE, BASTORE, CASTORE, SASTORE, POP, POP2, DUP, DUP_X1, DUP_X2, DUP2, DUP2_X1, DUP2_X2,
        SWAP, IADD, LADD, FADD, DADD, ISUB, LSUB, FSUB, DSUB, IMUL, LMUL, FMUL, DMUL, IDIV, LDIV,
        FDIV, DDIV, IREM, LREM, FREM, DREM, INEG, LNEG, FNEG, DNEG, ISHL, LSHL, ISHR, LSHR, IUSHR,
        LUSHR, IAND, LAND, (byte)IOR, (byte)LOR, (byte)IXOR, (byte)LXOR, (byte)I2L, (byte)I2F,
                (byte) I2D, (byte) L2I, (byte)L2F, (byte)L2D, (byte)F2I, (byte)F2L, (byte)F2D, (byte)D2I,
                (byte)D2L, (byte)D2F, (byte)I2B, (byte)I2C, (byte)I2S, (byte)LCMP, (byte)FCMPL, (byte)FCMPG,
                (byte)DCMPL, (byte)DCMPG, (byte)IRETURN, (byte)LRETURN, (byte)FRETURN, (byte)DRETURN, (byte)ARETURN,
                (byte)RETURN, (byte)ARRAYLENGTH, (byte)ATHROW, (byte)MONITORENTER, (byte)MONITOREXIT -> mv.visitInsn(current);
        case BIPUSH, SIPUSH, (byte)NEWARRAY -> mv.visitIntInsn(current, opcodeLookup.get(opcodes[++i]));
        case ILOAD, LLOAD, FLOAD, DLOAD, ALOAD, ISTORE, LSTORE, FSTORE, DSTORE, ASTORE, (byte)RET -> mv.visitVarInsn(current, opcodeLookup.get(opcodes[++i]));
        // Jump Case
        // Does this work for pre-computed jumps?
//        case (byte)IFEQ, (byte)IFNE, (byte)IFLT, (byte)IFGE, (byte)IFGT, (byte)IFLE, (byte)IF_ICMPEQ, (byte)IF_ICMPNE,
//                (byte)IF_ICMPLT, (byte)IF_ICMPGE, (byte)IF_ICMPGT, (byte)IF_ICMPLE, (byte)IF_ACMPEQ, (byte)IF_ACMPNE,
//                (byte)GOTO, (byte)JSR, (byte)IFNULL, (byte)IFNONNULL -> mv.visitVarInsn(current, opcodeLookup.get(opcodes[++i]));

        default -> System.err.println("Unknown opcode "+ current +" encountered");
      }
    }

    mv.visitMaxs(3, 3);
    mv.visitEnd();
  }

  void addStandardConstructor() {
    var mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
    mv.visitVarInsn(ALOAD, 0);
    mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
    mv.visitInsn(RETURN);
    mv.visitMaxs(1, 1);
    mv.visitEnd();
  }
}
