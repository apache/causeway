package org.ro.handler

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.JSON
import org.ro.core.DisplayManager
import org.ro.to.Service

@ImplicitReflectionSerializer
class MemberHandler : AbstractHandler(), IResponseHandler {

    override fun canHandle(jsonStr: String): Boolean {
        try {
            val service = JSON.parse(Service.serializer(), jsonStr)
            return true
        } catch (ex: Exception) {
            return false
        }
    }

    override fun doHandle(jsonStr: String) {
        val service = JSON.parse(Service.serializer(), jsonStr)
        val members = service.getMemberList()
        val mnu = DisplayManager.getMenu()
        val done: Boolean = mnu!!.init(service, members)
        if (done) {
            DisplayManager.amendMenu(mnu)
        }
    }

}