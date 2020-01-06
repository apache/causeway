package org.ro.handler

import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import org.ro.to.TransferObject
import org.ro.to.mb.Menubars

class MenuBarsHandler : BaseHandler() {

    @UnstableDefault
    override fun parse(response: String): TransferObject? {
        return Json.parse(Menubars.serializer(), response)
    }

}
