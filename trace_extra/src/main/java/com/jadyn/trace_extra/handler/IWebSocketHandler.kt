package com.jadyn.trace_extra.handler

import com.koushikdutta.async.http.WebSocket

interface IWebSocketHandler {

    fun handle(webSocket: WebSocket, message: String?)
}