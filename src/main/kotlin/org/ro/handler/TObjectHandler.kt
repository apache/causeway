package org.ro.handler

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.JSON
import org.ro.to.TObject

@ImplicitReflectionSerializer
class TObjectHandler : AbstractHandler(), IResponseHandler {

    override fun canHandle(jsonStr: String): Boolean {
        try {
            JSON.parse(TObject.serializer(), jsonStr)
            return true
        } catch (ex: Exception) {
            return false
        }
    }

    override fun doHandle(jsonStr: String) {
        val tObject = JSON.parse(TObject.serializer(), jsonStr)
        logEntry.obj = tObject
    }

}