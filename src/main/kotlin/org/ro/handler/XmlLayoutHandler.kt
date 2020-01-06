package org.ro.handler

import org.ro.core.Utils
import org.ro.layout.Layout
import org.ro.to.TransferObject
import org.ro.to.bs3.Grid
import org.ro.to.bs3.XmlHelper

class XmlLayoutHandler : BaseHandler() {

    override fun canHandle(response: String): Boolean {
        return Utils.isXml(response) && !response.contains("svg")
    }

    override fun parse(response: String): TransferObject? {
        val doc = XmlHelper().parseXml(response)
        val grid = Grid(doc)
        return Layout(grid)
    }

}
