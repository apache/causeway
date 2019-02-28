package org.ro.handler

import kotlinx.serialization.json.JSON
import org.ro.layout.Layout


class LayoutHandler : AbstractHandler(), IResponseHandler {

    override fun canHandle(jsonStr: String): Boolean {
        try {
            val layout = parse(jsonStr)
            return layout.row.size > 0
        } catch (ex: Exception) {
            return false
        }
    }

    override fun doHandle(jsonStr: String) {
        val layout = parse(jsonStr)
        logEntry.obj = layout
    }

    fun parse(jsonStr: String): Layout {
        return JSON.parse(Layout.serializer(), jsonStr)
    }

}
