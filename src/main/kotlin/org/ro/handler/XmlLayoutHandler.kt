package org.ro.handler

import kotlinx.serialization.UnstableDefault
import org.ro.layout.Layout
import org.ro.to.TransferObject
import org.ro.to.bs3.Grid
import org.w3c.dom.Document
import org.w3c.dom.parsing.DOMParser

class XmlLayoutHandler : BaseHandler(), IResponseHandler {

    override fun doHandle() {
        update()
    }

    @UnstableDefault
    override fun parse(response: String): TransferObject? {
        val doc = parseXml(response)
        val grid = Grid(doc)
        return Layout(grid)
    }

    fun parseXml(xmlStr: String): Document {
        val p = DOMParser()
        return p.parseFromString(xmlStr, "application/xml")
    }


}
