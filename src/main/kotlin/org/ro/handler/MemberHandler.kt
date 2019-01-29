package org.ro.handler

import org.ro.core.DisplayManager
import org.ro.to.Service

class MemberHandler : AbstractHandler(), IResponseHandler {

    override fun canHandle(jsonStr: String): Boolean {
        /*
        val members = jsonObj["members"]
        val extensions = jsonObj["extensions"].jsonObject
        val isService =  extensions["isService"]
        return (!members.isNull && !isService.isNull) */
        return false
    }

    override fun doHandle(jsonStr: String) {
        //val services = kotlinx.serialization.json.JSON.parse(Services.serializer(), jsonStr)
        val service = Service();
        val members = service.getMembers()
        val mnu = DisplayManager.getMenu()
        val done: Boolean = mnu!!.init(service, members)
        if (done) {
            DisplayManager.amendMenu(mnu)
        }
    }

}