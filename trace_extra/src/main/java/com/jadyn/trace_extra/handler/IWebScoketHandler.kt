package com.jadyn.trace_extra.handler

import com.koushikdutta.async.http.WebSocket

interface IWebScoketHandler {

    fun handle(webScoket: WebSocket, message: String?)
}