package org.ro.handler

import kotlinx.serialization.json.JSON
import org.ro.to.TObject

class TObjectHandler : AbstractHandler(), IResponseHandler {

    override fun canHandle(jsonStr: String): Boolean {
        try {
            parse(jsonStr)
            return true
        } catch (ex: Exception) {
            return false
        }
    }

    override fun doHandle(jsonStr: String) {
        val tObject = parse(jsonStr)
        logEntry.obj = tObject
    }

    fun parse(jsonStr: String): TObject {
        return JSON.nonstrict.parse(TObject.serializer(), jsonStr)
    }

}