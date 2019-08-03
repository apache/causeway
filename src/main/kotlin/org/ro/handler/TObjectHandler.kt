package org.ro.handler

import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.JSON
import org.ro.to.TransferObject
import org.ro.to.TObject

@UnstableDefault
class TObjectHandler : BaseHandler(), IResponseHandler {

    override fun doHandle() {
        update()
    }

    //@UseExperimental(kotlinx.serialization.UnstableDefault::class)
    override fun parse(jsonStr: String): TransferObject? {
        return JSON.parse(TObject.serializer(), jsonStr)
    }

}
