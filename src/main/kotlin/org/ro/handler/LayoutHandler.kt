package org.ro.handler

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.JSON
import org.ro.layout.Layout

@ImplicitReflectionSerializer
class LayoutHandler : AbstractHandler(), IResponseHandler {

    override fun canHandle(jsonStr: String): Boolean {
        try {
            val layout = JSON.parse(Layout.serializer(), jsonStr)
            return (layout.row != null && layout.row.size > 0)
        } catch (ex: Exception) {
            return false
        }
    }

    override fun doHandle(jsonStr: String) {
        val layout = JSON.parse(Layout.serializer(), jsonStr)
        logEntry.obj = layout
    }

}
