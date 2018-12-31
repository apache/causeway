package org.ro.handler

import kotlinx.serialization.json.JsonObject
import org.ro.layout.Layout

class LayoutHandler : AbstractHandler(), IResponseHandler {

    override fun canHandle(jsonObj: JsonObject): Boolean {
        val rows = jsonObj["row"].jsonArray
        return (!rows.isEmpty() && rows.size > 0)
    }

    override fun doHandle(jsonObj: JsonObject) {
        val layout = Layout(jsonObj)
        logEntry.obj = layout
    }

}
