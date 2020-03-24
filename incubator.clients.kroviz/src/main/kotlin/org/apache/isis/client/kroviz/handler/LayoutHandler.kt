package org.apache.isis.client.kroviz.handler

import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import org.apache.isis.client.kroviz.layout.Layout
import org.apache.isis.client.kroviz.to.Link
import org.apache.isis.client.kroviz.to.Links
import org.apache.isis.client.kroviz.to.Method
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

    fun parseProperties(response: String): TransferObject? {
        val prefix = "\"href\":"
        val hrefList = mutableListOf<String>()
        val lines = response.split("\n")
        lines.forEach { l ->
            if (l.trim().startsWith(prefix) && l.contains("/properties/")) {
                hrefList.add(l)
            }
        }
        val propertyLinks = mutableListOf<Link>()
        hrefList.forEach { h ->
            val href = h.replace(prefix, "").trim().drop(1).dropLast(1)
            val link = Link(method = Method.GET.operation, href = href)
            propertyLinks.add(link)
        }
        val links = Links(propertyLinks)
        return links
    }

}
