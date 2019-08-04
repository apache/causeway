package org.ro.handler

import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.JSON
import org.ro.to.Property
import org.ro.to.TransferObject

@UnstableDefault
class PropertyHandler : BaseHandler(), IResponseHandler {

    override fun doHandle() {
        update()
    }

    //@UseExperimental(kotlinx.serialization.UnstableDefault::class)
    override fun parse(jsonStr: String): TransferObject? {
        return JSON.parse(Property.serializer(), jsonStr)
    }

}
