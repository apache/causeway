package org.ro.handler

import kotlinx.serialization.json.JsonObject
import org.ro.core.DisplayManager
import org.ro.to.Service

class MemberHandler : AbstractHandler(), IResponseHandler {

    override fun canHandle(jsonObj: JsonObject): Boolean {
        val members = jsonObj["members"]
        val extensions = jsonObj["extensions"].jsonObject
        val isService =  extensions["isService"]
        return (!members.isNull && !isService.isNull)
    }

    override fun doHandle(jsonObj: JsonObject): Unit {
        val service = Service(jsonObj)
        val members = service.getMembers()
        val mnu = DisplayManager.getMenu()
        val done: Boolean = mnu!!.init(service, members)
        if (done) {
            DisplayManager.amendMenu(mnu)
        }
    }

}