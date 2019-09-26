package org.ro.org.ro.handler

import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import org.ro.handler.BaseHandler
import org.ro.handler.IResponseHandler
import org.ro.org.ro.to.User
import org.ro.to.TransferObject

class UserHandler : BaseHandler(), IResponseHandler {

    override fun doHandle() {
        update()
    }

    @UnstableDefault
    override fun parse(jsonStr: String): TransferObject? {
        return Json.parse(User.serializer(), jsonStr)
    }

}
