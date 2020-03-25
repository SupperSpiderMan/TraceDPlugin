package com.jadyn.ai.tracedplugin;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 * @version:
 * @FileDescription:
 * @Author:Jing
 * @Since:2020/3/20
 * @ChangeList:
 */
public class MyClassVisitor extends ClassVisitor implements Opcodes {

    public MyClassVisitor(int api, ClassVisitor classVisitor) {
        super(api, classVisitor);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        cv.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, name, descriptor, signature, exceptions);
        if (name.equals("<init>") || mv == null) {
            return mv;
        }
        return new MyMethodVisitor(Opcodes.ASM5, mv, access, name, descriptor);
    }

    static class MyMethodVisitor extends AdviceAdapter {

        MyMethodVisitor(int api, MethodVisitor methodVisitor, int access, String name, String descriptor) {
            super(api, methodVisitor, access, name, descriptor);
        }

        @Override
        protected void onMethodEnter() {
            super.onMethodEnter();
            // 常量推到操作数栈
            mv.visitMethodInsn(Opcodes.H_INVOKESTATIC,
                    "android/os/Trace", "beginSection",
                    "(Ljava/lang/String;)V", false);
            mv.visitLdcInsn("");
        }

        @Override
        protected void onMethodExit(int opcode) {
            super.onMethodExit(opcode);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "android/os/Trace"
                    , "endSection", "()V", false);
        }
    }

}
