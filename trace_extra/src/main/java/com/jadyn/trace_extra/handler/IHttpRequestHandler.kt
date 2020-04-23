package com.jadyn.trace_extra.handler

interface IHttpRequestHandler {

    fun handle(path: String): Map<String, String?>
}