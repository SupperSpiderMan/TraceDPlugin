package com.jadyn.ai.trace_plugin;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class TraceClassVisitor extends ClassVisitor {

    private int mApi;
    private ClassVisitor mCv;
    private TraceConfig mTraceConfig;
    private String mClassName = "";
    private boolean mIsAbsClassOrInterface = false;
    private boolean mIsBeatClass = false;
    private boolean mIsConfigTraceClass;

    public TraceClassVisitor(int api, ClassVisitor cv, TraceConfig traceConfig) {
        super(api, cv);
        this.mApi = api;
        this.mCv = cv;
        this.mTraceConfig = traceConfig;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.mClassName = name;
        if ((access & Opcodes.ACC_ABSTRACT) > 0 || (access & Opcodes.ACC_INTERFACE) > 0) {
            this.mIsAbsClassOrInterface = true;
        }

        //插桩代码所属类
//        String resultClassName = name.replace(".", "/")
//        if (resultClassName == mTraceConfig.mBeatClass) {
//            this.mIsBeatClass = true
//        }
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        boolean isConstructor = MethodFilter.isConstructor(name);
        if (mIsAbsClassOrInterface || isConstructor) {
            return super.visitMethod(access, name, descriptor, signature, exceptions);
        } else {
            MethodVisitor mv = mCv.visitMethod(access, name, descriptor, signature, exceptions);
            return new TraceMethodVisitor(mApi, mv, access, name, descriptor, mClassName, mTraceConfig);
        }
    }
}
