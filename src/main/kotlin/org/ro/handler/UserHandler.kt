package org.ro.handler

import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import org.ro.to.TransferObject
import org.ro.to.User

class UserHandler : BaseHandler(), IResponseHandler {

    @UnstableDefault
    override fun parse(response: String): TransferObject? {
        return Json.parse(User.serializer(), response)
    }

}
