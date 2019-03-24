package org.ro.handler

import kotlinx.serialization.json.JSON
import org.ro.core.Menu
import org.ro.core.event.NavigationObserver
import org.ro.to.Result

/** handles services result */
class ResultHandler : AbstractHandler(), IResponseHandler {

    override fun canHandle(jsonStr: String): Boolean {
        var answer = false
        try {
            val obj = parse(jsonStr)
            logEntry.obj = obj
            answer = true
        } catch (ex: Exception) {
        }
        return answer
    }

    override fun doHandle(jsonStr: String) {
        val services = logEntry.obj as Result
        val values = services.valueList()
        Menu.limit = values.size
        val observer = NavigationObserver(logEntry.url)
        for (l in values) {
            l.invoke(observer)
        }
    }

    fun parse(jsonStr: String): org.ro.to.Result {
        return JSON.parse(Result.serializer(), jsonStr)
    }
}