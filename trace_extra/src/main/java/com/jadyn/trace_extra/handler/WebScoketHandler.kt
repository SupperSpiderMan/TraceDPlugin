package com.jadyn.trace_extra.handler

import com.jadyn.trace_extra.MethodTraceServerManager.isActiveTraceMan
import com.jadyn.trace_extra.utils.LogUtil
import com.jadyn.trace_extra.producer.DataProducer
import com.jadyn.trace_extra.producer.module.appInfo.AppInfo
import com.jadyn.trace_extra.producer.module.methodcost.MethodCostHelper
import com.koushikdutta.async.http.WebSocket
import org.json.JSONObject

class WebScoketHandler : IWebScoketHandler {

    override fun handle(webScoket: WebSocket, message: String?) {
        val obj = JSONObject(message)
        val moduleName = obj["moduleName"]

        when (moduleName) {
            "OnlineMessage" -> {
                LogUtil.i("接收到消息：传输设备基本信息")
                DataProducer.producerAppInfo(AppInfo())
            }
            "StartMethodCost" -> {
                LogUtil.i("接收到消息：开始方法耗时统计")
                isActiveTraceMan = true
                MethodCostHelper.startMethodCost()
            }
            "EndMethodCost" -> {
                LogUtil.i("接收到消息：结束方法耗时统计")
                isActiveTraceMan = false
                MethodCostHelper.endMethodCost()
            }

        }


        LogUtil.detail("接到浏览器消息内容:$message")
    }
}