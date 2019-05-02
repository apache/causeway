package org.ro.handler

import kotlinx.serialization.json.JSON
import org.ro.to.TObject

class TObjectHandler : AbstractHandler(), IResponseHandler {

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
    }

    fun parse(jsonStr: String): TObject {
        return JSON.nonstrict.parse(TObject.serializer(), jsonStr)
    }

}