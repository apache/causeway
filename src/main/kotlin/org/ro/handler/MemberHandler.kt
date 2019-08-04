package org.ro.handler

import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import org.ro.to.Member
import org.ro.to.TransferObject

@UnstableDefault
class MemberHandler : BaseHandler(), IResponseHandler {

    override fun doHandle() {
        console.log("[MemberHandler.doHandle()] has no body")
    }

    //@UseExperimental(kotlinx.serialization.UnstableDefault::class)
    override fun parse(jsonStr: String): TransferObject? {
        return Json.parse(Member.serializer(), jsonStr)
    }
}
