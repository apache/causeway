package org.apache.isis.client.kroviz.handler

import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import org.apache.isis.client.kroviz.to.Member
import org.apache.isis.client.kroviz.to.TransferObject

class MemberHandler :org.apache.isis.client.kroviz.handler.BaseHandler() {

    @UnstableDefault
    override fun parse(response: String):TransferObject? {
        return Json.parse(Member.serializer(), response)
    }

}
