package org.ro.handler

import kotlinx.serialization.json.JSON
import org.ro.core.Menu
import org.ro.core.event.NavigationObserver
import org.ro.to.Result

/** handles services result */
class ServicesHandler : AbstractHandler(), IResponseHandler {

    override fun canHandle(jsonStr: String): Boolean {
        try {
            parse(jsonStr)
            return true
        } catch (ex: Exception) {
            return false
        }
    }

    override fun doHandle(jsonStr: String) {
        val services = parse(jsonStr)
        val values = services.valueList()
        Menu.limit = values.size
        val observer = NavigationObserver(logEntry.url)
        for (l in values) {
//            console.log("[ServicesHandler.doHandle -> invoke]: $l")
            l.invoke(observer)
        }
    }

    fun parse(jsonStr: String): org.ro.to.Result {
        return JSON.parse(Result.serializer(), jsonStr)
    }
}