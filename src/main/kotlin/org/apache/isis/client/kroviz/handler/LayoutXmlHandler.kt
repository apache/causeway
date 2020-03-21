package org.apache.isis.client.kroviz.handler

import org.apache.isis.client.kroviz.to.TransferObject
import org.apache.isis.client.kroviz.to.bs3.Grid
import org.apache.isis.client.kroviz.utils.XmlHelper

class LayoutXmlHandler : org.apache.isis.client.kroviz.handler.BaseHandler() {

    override fun canHandle(response: String): Boolean {
        val isLayoutXml = XmlHelper.isXml(response)
                && logEntry.url.endsWith("layout")
        if (isLayoutXml) {
            return super.canHandle(response)
        }
        return false
    }

    override fun parse(response: String): TransferObject? {
        val doc = XmlHelper.parseXml(response)
        return Grid(doc)
    }

}
