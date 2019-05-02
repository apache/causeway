package org.ro.handler

import kotlinx.serialization.json.JSON
import org.ro.to.HttpError
import org.ro.view.ErrorAlert

class HttpErrorHandler : AbstractHandler(), IResponseHandler {
    override fun canHandle(jsonStr: String): Boolean {
        var answer = false
        try {
            val obj = parse(jsonStr)
            logEntry.setObj(obj)
            answer = true
        } catch (ex: Exception) {
        }
        return answer
    }

    override fun doHandle() {
        val e = logEntry.getObj() as HttpError
        ErrorAlert(e).open()
    }

    fun parse(jsonStr: String): HttpError {
        return JSON.parse(HttpError.serializer(), jsonStr)
    }
}