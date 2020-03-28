package com.jadyn.trace_extra

import android.os.Trace

/**
 *JadynAi since 2020/3/28
 */
fun trace(sectionName: String) {
    Trace.beginSection(sectionName)
}

fun end() {
    Trace.endSection()
}