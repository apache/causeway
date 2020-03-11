package org.ro.to.bs3

import org.ro.to.TransferObject
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.asList

/**
 * For the Wicket Viewer the following layout is used:
 * * rows[0] (head) contains the object title and actions
 * * rows[1] contains data, tabs, collections, etc.
 * * there may be N other rows as well
 * Please note, that rows may be children of Tab as well (recursive)
 */
class Grid(document: Document) : TransferObject {
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

    fun getPropertyList(): List<Property> {
        val list = mutableListOf<Property>()
        rows.forEach { c ->
            list.addAll(c.getPropertyList())
        }
        return list
    }

}
