package org.ro.handler

import kotlinx.serialization.ImplicitReflectionSerializer

@ImplicitReflectionSerializer
class DefaultHandler : AbstractHandler(), IResponseHandler {

    override fun canHandle(jsonStr: String): Boolean {
        return true
    }

    override fun doHandle(jsonStr: String) {
        val url = logEntry.url
        if (jsonStr.isEmpty()) {
            console.log("json null for: $url")
        } else {
            console.log("No Handler found for: $url")
        }
    }

}