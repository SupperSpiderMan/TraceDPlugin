package com.jadyn.trace_extra.model

import com.jadyn.trace_extra.utils.JsonUtil

class Message {

    val SUCCESS = 1
    val DEFAULT_FAIL = 0

    var code: Int = 0
    var message: String
    var data: DataWithName? = null


    constructor(errorMessage: String) {
        this.code = DEFAULT_FAIL
        this.message = errorMessage
        this.data = null
    }

    constructor(moduleName: String, data: Any) {
        this.code = SUCCESS
        this.message = "success"
        this.data = DataWithName(moduleName, data)
    }

    class DataWithName(var moduleName: String, var payload: Any)

    override fun toString(): String {
        return JsonUtil.toJson(this, false)
    }
}