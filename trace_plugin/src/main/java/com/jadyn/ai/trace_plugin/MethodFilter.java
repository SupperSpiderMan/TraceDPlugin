package com.jadyn.ai.trace_plugin;

/**
 * JadynAi since 2020/4/23
 */
class MethodFilter {
    static boolean isConstructor(String methodName) {
        return methodName.contains("<init>");
    }
}
