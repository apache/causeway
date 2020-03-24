package org.apache.isis.client.kroviz.handler

import org.apache.isis.client.kroviz.utils.XmlHelper

class DiagramHandler : BaseHandler() {

    override fun canHandle(response: String): Boolean {
        return XmlHelper.isXml(response) && response.contains("svg")
    }

}
