package com.jadyn.ai.extension

class TraceDExtension {
    // 白名单或者黑名单模式，默认白名单。如果名单为空，不管什么模式，都全部插桩
    boolean isWhiteListMode
    boolean enable
    boolean isTraceJar
    String pacListFile

    TraceDExtension() {
        enable = true
        isWhiteListMode = true
        isTraceJar = true
        pacListFile = ""
    }
}