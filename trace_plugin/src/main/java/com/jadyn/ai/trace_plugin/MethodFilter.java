package com.jadyn.ai.trace_plugin;

/**
 * JadynAi since 2020/4/23
 */
public class MethodFilter {
    public static boolean isConstructor(String methodName) {
        return methodName.contains("<init>");
    }
}
