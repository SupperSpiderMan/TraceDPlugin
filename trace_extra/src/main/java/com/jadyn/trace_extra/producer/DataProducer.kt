package com.jadyn.trace_extra.producer

import com.jadyn.trace_extra.MethodTraceServerManager
import com.jadyn.trace_extra.MethodTraceServerManager.APPINFO
import com.jadyn.trace_extra.MethodTraceServerManager.METHODCOST
import com.jadyn.trace_extra.producer.module.appInfo.AppInfo
import com.jadyn.trace_extra.producer.module.appInfo.AppInfoProducer
import com.jadyn.trace_extra.producer.module.methodcost.MethodCostProducer
import com.jadyn.trace_extra.producer.module.methodcost.MethodInfo

class DataProducer {

    companion object {

        fun producerAppInfo(appInfo: AppInfo) {
            MethodTraceServerManager.getModule<AppInfoProducer>(APPINFO).produce(appInfo)
        }

        fun producerMethodCostInfo(methodCostInfo: List<MethodInfo>) {
            MethodTraceServerManager.getModule<MethodCostProducer>(METHODCOST)
                .produce(methodCostInfo)
        }
    }
}