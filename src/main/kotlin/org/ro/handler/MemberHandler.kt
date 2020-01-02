package org.ro.handler

import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import org.ro.to.Member
import org.ro.to.TransferObject

class MemberHandler : BaseHandler(), IResponseHandler {

    @UnstableDefault
    override fun parse(response: String): TransferObject? {
        return Json.parse(Member.serializer(), response)
    }
}
