package org.ro.handler

import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import org.ro.to.TransferObject
import org.ro.to.Version

class VersionHandler: BaseHandler() {

    @UnstableDefault
    override fun parse(response: String): TransferObject? {
        return Json.parse(Version.serializer(), response)
    }

}
