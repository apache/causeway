package org.ro.handler

import kotlinx.serialization.json.JsonObject
import org.ro.to.TObject

class TObjectHandler : AbstractHandler(), IResponseHandler {

    override fun canHandle(jsonObj: JsonObject): Boolean {
        return hasMembers(jsonObj) && !isService(jsonObj)
    }

    override fun doHandle(jsonObj: JsonObject) {
        val tObj = TObject(jsonObj)
        logEntry.obj = tObj
    }

}

