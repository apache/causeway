package org.ro.handler

import org.ro.core.Utils

class DiagramHandler : BaseHandler(), IResponseHandler {

    override fun canHandle(response: String): Boolean {
        return Utils.isXml(response) && response.contains("svg")
    }

}
