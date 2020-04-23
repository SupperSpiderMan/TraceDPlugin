package com.jadyn.trace_extra.utils

import com.google.gson.GsonBuilder

class JsonUtil {

    companion object {
        fun toJson(any: Any?, excludeFieldsWithoutExposeAnnotation: Boolean): String {
            return any?.let {
                createGsonBuilder(excludeFieldsWithoutExposeAnnotation).create().toJson(any)
            } ?: ""
        }

        private fun createGsonBuilder(excludeFieldsWithoutExposeAnnotation: Boolean): GsonBuilder {
            return GsonBuilder().apply {
                if (excludeFieldsWithoutExposeAnnotation) {
                    excludeFieldsWithoutExposeAnnotation()
                }
                disableHtmlEscaping()
            }
        }
    }

}