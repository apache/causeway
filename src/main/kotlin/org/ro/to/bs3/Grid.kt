package org.ro.to.bs3

import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.asList

class Grid(document: Document) {
    var rows = ArrayList<Row>()

    init {
        val root = document.firstChild!!
        val kids = root.childNodes
        val rowNodes = kids.asList()
        val rowList = rowNodes.filter { it.nodeName.equals("bs3:row") }
        for (n: Node in rowList) {
            val row = Row(n)
            rows.add(row)
        }
    }
}
