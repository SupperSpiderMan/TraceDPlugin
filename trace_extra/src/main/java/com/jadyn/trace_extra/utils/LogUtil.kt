package com.jadyn.trace_extra.utils

import android.util.Log
import com.jadyn.trace_extra.MethodTraceServerManager
import com.jadyn.trace_extra.MethodTraceServerManager.DEBUG_SERVER_TAG
import com.jadyn.trace_extra.MethodTraceServerManager.MTM_LOG_DETAIL

class LogUtil {

    companion object {


        @JvmStatic
        fun detail(message: String?) {
            if (MethodTraceServerManager.logLevel == MTM_LOG_DETAIL) {
                Log.i(DEBUG_SERVER_TAG, message)
            }
        }

        @JvmStatic
        fun i(message: String?) {
            Log.i(DEBUG_SERVER_TAG, message)
        }


        @JvmStatic
        fun e(message: String?) {
            Log.e(DEBUG_SERVER_TAG, message)
        }
    }
}