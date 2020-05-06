package com.jadyn.trace_extra

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.WifiManager
import android.os.Looper
import com.jadyn.trace_extra.utils.LogUtil
import com.jadyn.trace_extra.consumer.DataConsumer
import com.jadyn.trace_extra.handler.HttpRequestHandler
import com.jadyn.trace_extra.handler.WebSocketHandler
import com.jadyn.trace_extra.producer.module.appInfo.AppInfoProducer
import com.jadyn.trace_extra.producer.module.methodcost.MethodCostProducer
import com.koushikdutta.async.http.WebSocket
import com.koushikdutta.async.http.server.AsyncHttpServerRequest
import com.koushikdutta.async.http.server.AsyncHttpServerResponse

object MethodTraceServerManager {

    const val DEBUG_SERVER_TAG = "MethodTraceD"
    const val DEBUG_SERVER_PORT = 5392

    const val APPINFO = "appInfo"
    const val METHODCOST = "methodCost"

    private var isServerRunning = false
    private var debugServer: TraceDServer? = null
    private var dataConsumer: DataConsumer? = null

    private var dataModules = HashMap<String, Any>()

    @JvmField
    var isActiveTraceMan = false

    const val MTM_LOG_IMPORTANT = 1
    const val MTM_LOG_DETAIL = 2

    var logLevel = MTM_LOG_IMPORTANT

    init {
        dataModules[APPINFO] = AppInfoProducer()
        dataModules[METHODCOST] = MethodCostProducer()
    }


    fun <T> getModule(name: String): T {
        return dataModules[name] as T
    }


    /**
     * 开启服务
     */
    @Synchronized
    @JvmOverloads
    fun startService(context: Context, port: Int = DEBUG_SERVER_PORT) {
        if (isServerRunning) {
            return
        }

        isServerRunning = true

        debugServer = TraceDServer(port)

        setServerCallback(context)

        dataConsumer = DataConsumer(debugServer!!)

        debugServer?.start()

        dataConsumer?.observe()

        LogUtil.i("MethodTrace Server is running")
        LogUtil.i("http://${getIPAddress(context)}:$port/index.html")

    }


    private fun setServerCallback(context: Context) {
        val httpRequestHandler = HttpRequestHandler(context, "methodtraceman")
        val webSocketHandler = WebSocketHandler()

        debugServer?.serverCallback = object : TraceDServer.ServerCallback {
            override fun onHttpRequest(
                request: AsyncHttpServerRequest,
                response: AsyncHttpServerResponse
            ) {
                checkThread()
                try {
                    val map = httpRequestHandler.handle(request.path)
                    response.send(map["mimeType"], map["payload"])

                } catch (throwable: Throwable) {
                    LogUtil.e(throwable.toString())
                }
            }

            override fun onWebSocketRequest(webSocket: WebSocket, messageFromClient: String) {
                checkThread()
                webSocketHandler.handle(webSocket, messageFromClient)
            }
        }
    }


    /**
     * 关闭服务
     */
    @Synchronized
    fun stopService() {
        debugServer?.stop()
        debugServer = null


        dataConsumer?.clearObserve()
        dataConsumer = null

        isServerRunning = false
        LogUtil.i("MethodTrace Server Stopped.")
    }

    private fun isInMainThread(): Boolean {
        return Looper.myLooper() == Looper.getMainLooper()
    }

    private fun checkThread() {
        if (isInMainThread()) {
            throw IllegalStateException("trace man service must execute on work thread!")
        }
    }

    private fun getIPAddress(context: Context): String {
        @SuppressLint("WifiManagerPotentialLeak")
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val ipAddress = wifiManager.connectionInfo?.ipAddress ?: 0

        @SuppressLint("DefaultLocale")
        val formattedIpAddress = String.format(
            "%d.%d.%d.%d",
            ipAddress and 0xff,
            ipAddress shr 8 and 0xff,
            ipAddress shr 16 and 0xff,
            ipAddress shr 24 and 0xff
        )
        return formattedIpAddress
    }

}