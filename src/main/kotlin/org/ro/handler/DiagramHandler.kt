package org.ro.handler

import org.w3c.dom.parsing.DOMParser
import kotlin.browser.document

class DiagramHandler : BaseHandler(), IResponseHandler {

    override fun doHandle() {
        update()
    }

    override fun canHandle(xhrResponse: String): Boolean {
        val p = DOMParser()
        val svg = p.parseFromString(xhrResponse, "image/svg+xml")
        val dp = document.getElementById("diagramPanel")
        dp.asDynamic().appendChild(svg.documentElement)
        return true
    }

}
