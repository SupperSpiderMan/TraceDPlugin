package com.jadyn.ai.trace_plugin;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 * @version:
 * @FileDescription:
 * @Author:Jing
 * @Since:2020/3/23
 * @ChangeList:
 */
public class TraceMethodVisitor extends AdviceAdapter {

    private String mClassName;
    private String mName;
    private String mMethodName;
    private int mMaxSectionNameLength = 127;

    protected TraceMethodVisitor(int api, MethodVisitor methodVisitor, int access, String name,
                                 String descriptor,
                                 String className,
                                 TraceConfig traceConfig) {
        super(api, methodVisitor, access, name, descriptor);
        mClassName = className;
        mName = name;
        if (descriptor == null || (access & Opcodes.ACC_NATIVE) != 0) {
            mMethodName = className + "." + name;
        } else {
            mMethodName = className + "." + name + "." + descriptor;
        }
    }

    @Override
    protected void onMethodEnter() {
        super.onMethodEnter();
        String methodName = generatorMethodName();
        mv.visitLdcInsn(methodName);
        String invokeClass = "android/os/Trace";
        String invokeMethodName = "beginTrace";
        String paramDescriptor = "(Ljava/lang/String;)V";
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, invokeClass, invokeMethodName, paramDescriptor, false);
        System.out.println("method trace: " + methodName);
    }

    @Override
    protected void onMethodExit(int opcode) {
        super.onMethodExit(opcode);
        mv.visitLdcInsn(generatorMethodName());
        String invokeClass = "android/os/Trace";
        String invokeMethodName = "endTrace";
        String paramDescriptor = "()V";
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, invokeClass, invokeMethodName, paramDescriptor, false);
    }

    private String generatorMethodName() {
        String sectionName = mMethodName;
        int length = sectionName.length();
        if (length > mMaxSectionNameLength) {
            // 2020/3/23-18:28 从第一个参数开始截断
            int paramsIndex = sectionName.indexOf("(");
            sectionName = sectionName.substring(0, paramsIndex);
            if (sectionName.length() > mMaxSectionNameLength) {
                sectionName = sectionName.substring(sectionName.length() - mMaxSectionNameLength);
            }
        }
        return sectionName;
    }
}
