package com.jadyn.ai.trace_plugin;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

import static com.jadyn.ai.trace_plugin.TraceBuildConstants.MAX_SECTION_NAME_LEN;

public class TraceMethodVisitor extends AdviceAdapter {

    private String mClassName;
    private String mName;
    private String mMethodName;

    TraceMethodVisitor(int api, MethodVisitor methodVisitor, int access, String name,
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
        mv.visitLdcInsn(generatorMethodName());
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, TraceBuildConstants.TRACE_CLASS, TraceBuildConstants.TRACE_METHOD_START,
                "(Ljava/lang/String;)V", false);
    }

    @Override
    protected void onMethodExit(int opcode) {
        // 2020/4/23-17:34 如果没有要插入的函数没有参数，就不需要这一句
        mv.visitLdcInsn(generatorMethodName());
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, TraceBuildConstants.TRACE_CLASS, TraceBuildConstants.TRACE_METHOD_END,
                "(Ljava/lang/String;)V", false);
    }

    private String generatorMethodName() {
        String sectionName = mMethodName;
        int length = sectionName.length();
        if (length > MAX_SECTION_NAME_LEN) {
            // 2020/3/23-18:28 从第一个参数开始截断
            int paramsIndex = sectionName.indexOf("(");
            sectionName = sectionName.substring(0, paramsIndex);
            if (sectionName.length() > MAX_SECTION_NAME_LEN) {
                sectionName = sectionName.substring(sectionName.length() - MAX_SECTION_NAME_LEN);
            }
        }
        return sectionName;
    }
}
