package org.ro.handler

import kotlinx.serialization.json.JSON
import org.ro.core.TransferObject
import org.ro.to.Member

class MemberHandler : AbstractHandler(), IResponseHandler {

    override fun doHandle() {
        console.log("[MemberHandler.doHandle()] has no body")
    }

    override fun parse(jsonStr: String): TransferObject? {
        return JSON.parse(Member.serializer(), jsonStr)
    }
}