package org.ro.handler

import org.ro.to.TransferObject
import org.ro.to.bs3.Grid
import org.ro.utils.XmlHelper

class LayoutXmlHandler : BaseHandler() {

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
