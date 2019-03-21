package org.ro.handler

import kotlinx.serialization.json.JSON
import org.ro.to.HttpError
import org.ro.view.ErrorAlert

class HttpErrorHandler : AbstractHandler(), IResponseHandler {
    override fun canHandle(jsonStr: String): Boolean {
        var answer = false
        try {
            parse(jsonStr)
            answer = true
        } catch (ex: Exception) {
        }
        return answer
    }

    override fun doHandle(jsonStr: String) {
        val e = parse(jsonStr)
        ErrorAlert(e).open()
    }

    fun parse(jsonStr: String): HttpError {
        return JSON.parse(HttpError.serializer(), jsonStr)
    }
}