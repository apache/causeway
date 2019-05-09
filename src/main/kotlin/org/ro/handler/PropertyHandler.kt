package org.ro.handler

import kotlinx.serialization.json.JSON
import org.ro.core.TransferObject
import org.ro.core.event.ListObserver
import org.ro.to.Property

class PropertyHandler : BaseHandler(), IResponseHandler {

    override fun doHandle() {
        //TODO move link invocation to observer
        val p = logEntry.getObj() as Property
        val descLink = p.descriptionLink()
        val obs: ListObserver
        if (logEntry.observer == null) {
            obs = ListObserver()
            console.log("[PropertyHandler.doHandle] empty observer set to ListObserver")
        } else {
            obs = logEntry.observer as ListObserver
        }
        if (descLink != null) {
            descLink.invoke(obs)
        }
        obs.list.handleProperty(p)
    }

    //@UseExperimental(kotlinx.serialization.UnstableDefault::class)
    override fun parse(jsonStr: String): TransferObject? {
        return JSON.parse(Property.serializer(), jsonStr)
    }

}