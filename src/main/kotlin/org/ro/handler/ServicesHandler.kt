package org.ro.handler

import kotlinx.serialization.json.JSON
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
        //FIXME use Observer instead? services is the 'up' url of each individual service
//        DisplayManager.setMenu(menu)
        for (l in values) {
            console.log("[ServiceHandler.doHandle -> invoke]: $l")
            l.invoke()
        }
    }

    fun parse(jsonStr: String): org.ro.to.Result {
        return JSON.parse(Result.serializer(), jsonStr)
    }
}