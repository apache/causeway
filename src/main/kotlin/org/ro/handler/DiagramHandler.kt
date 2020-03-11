package org.ro.handler

import org.ro.utils.XmlHelper

class DiagramHandler : BaseHandler() {

    override fun canHandle(response: String): Boolean {
        return XmlHelper.isXml(response) && response.contains("svg")
    }

}
