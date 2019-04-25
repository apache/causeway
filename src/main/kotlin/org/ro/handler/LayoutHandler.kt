package org.ro.handler

import kotlinx.serialization.json.JSON
import org.ro.layout.Layout

class LayoutHandler : AbstractHandler(), IResponseHandler {

    override fun canHandle(jsonStr: String): Boolean {
        var answer = false
        try {
            val layout = parse(jsonStr)
            logEntry.obj = layout
            answer = true
        } catch (ex: Exception) {
        }
        return answer;
    }

    override fun doHandle() {
        val obs = logEntry.observer
        obs!!.update(logEntry)
    }

    fun parse(jsonStr: String): Layout {
        return JSON.parse(Layout.serializer(), jsonStr)
    }

}
