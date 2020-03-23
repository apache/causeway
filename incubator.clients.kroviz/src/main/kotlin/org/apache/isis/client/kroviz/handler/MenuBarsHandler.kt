package org.apache.isis.client.kroviz.handler

import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import org.apache.isis.client.kroviz.to.TransferObject
import org.apache.isis.client.kroviz.to.mb.Menubars

class MenuBarsHandler : BaseHandler() {

    @UnstableDefault
    override fun parse(response: String): TransferObject? {
        return Json.parse(Menubars.serializer(), response)
    }

}
