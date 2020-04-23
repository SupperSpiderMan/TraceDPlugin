package com.jadyn.trace_extra.producer.module.appInfo

import android.os.Build

class AppInfo {

    val appInfo = ArrayList<String>().apply {
        add("机型：${Build.BRAND} ${Build.MODEL}")
        add("系统版本：${Build.VERSION.SDK_INT}")
//        add("包名：${Build.)}")
//        add("App版本：${AppUtil.getVersionCode()}")

    }
}