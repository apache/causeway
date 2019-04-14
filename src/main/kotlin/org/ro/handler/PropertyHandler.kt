package org.ro.handler

import kotlinx.serialization.json.JSON
import org.ro.core.event.ListObserver
import org.ro.to.Property

class PropertyHandler : AbstractHandler(), IResponseHandler {

    override fun canHandle(jsonStr: String): Boolean {
        var answer = false
//        console.log("[PropertyHandler.canHandle] 1 ${logEntry.observer}")
        try {
            val p = parse(jsonStr)
            logEntry.obj = p
            val link = p.descriptionLink();
            answer = link != null
        } catch (ex: Exception) {
        }
//        console.log("[PropertyHandler.canHandle] 2 ${logEntry.observer}")
        return answer
    }

    override fun doHandle() {
        val p = logEntry.obj as Property
        val link = p.descriptionLink()
        var obs = logEntry.observer
        console.log("[PropertyHandler.doHandle] $obs")
        if (link != null) {
            link.invoke(obs)
            /* */
            if (obs == null) {  // happened during PropertyHandlerTest, cascading calls
                logEntry.initListObserver()
                obs = logEntry.observer
            } /**/
        }
        obs = obs as ListObserver
        obs.list.handleProperty(p)
    }

    fun parse(jsonStr: String): Property {
        return JSON.parse(Property.serializer(), jsonStr)
    }

}