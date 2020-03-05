package org.ro.handler

import org.ro.utils.Utils

class DiagramHandler : BaseHandler() {

    override fun canHandle(response: String): Boolean {
        return Utils.isXml(response) && response.contains("svg")
    }

}
