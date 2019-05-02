package org.ro.handler

import kotlinx.serialization.json.JSON
import org.ro.layout.Layout

class LayoutHandler : AbstractHandler(), IResponseHandler {

    override fun canHandle(jsonStr: String): Boolean {
        var answer = false
        try {
            val obj = parse(jsonStr)
            logEntry.setObj(obj)
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
