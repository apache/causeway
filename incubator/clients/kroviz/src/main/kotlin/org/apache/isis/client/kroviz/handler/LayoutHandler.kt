package org.apache.isis.client.kroviz.handler

import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import org.apache.isis.client.kroviz.layout.Layout
import org.apache.isis.client.kroviz.to.TransferObject
import org.apache.isis.client.kroviz.utils.XmlHelper

class LayoutHandler : org.apache.isis.client.kroviz.handler.BaseHandler() {

    override fun canHandle(response: String): Boolean {
        val isJsonLayout = !XmlHelper.isXml(response)
                && logEntry.url.endsWith("layout")
        if (isJsonLayout) {
            return super.canHandle(response)
        }
        return false
    }

    @UnstableDefault
    override fun parse(response: String): TransferObject? {
        return Json.parse(Layout.serializer(), response)
    }

}
