package org.apache.isis.client.kroviz.handler

import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import org.apache.isis.client.kroviz.to.TransferObject
import org.apache.isis.client.kroviz.to.Version

class VersionHandler : BaseHandler() {

    @UnstableDefault
    override fun parse(response: String): TransferObject? {
        return Json.parse(Version.serializer(), response)
    }

}
