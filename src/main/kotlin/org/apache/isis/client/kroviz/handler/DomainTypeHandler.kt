package org.apache.isis.client.kroviz.handler

import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import org.apache.isis.client.kroviz.to.DomainType
import org.apache.isis.client.kroviz.to.TransferObject

class DomainTypeHandler : BaseHandler() {

    @UnstableDefault
    override fun parse(response: String): TransferObject? {
        return Json.parse(DomainType.serializer(), response)
    }

}
