package org.ro.handler

import kotlinx.serialization.json.JsonObject

class DefaultHandler : AbstractHandler(), IResponseHandler {

    override fun canHandle(jsonObj: JsonObject): Boolean {
        return true
    }

    override fun doHandle(jsonObj: JsonObject) {
        val url =logEntry.url
        if (jsonObj.isEmpty()) {
            console.log("json null for: $url")
        } else {
            console.log("No Handler found for: $url")
        }
    }

}