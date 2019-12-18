package org.ro.to.bs3

import org.w3c.dom.Node
import org.w3c.dom.asList

class Tab(node: Node) {
    var rows = mutableListOf<Row>()
    var name: String
//    var cssClass: String

    init {
        val dyNode = node.asDynamic()
        name = dyNode.getAttribute("name") as String
//        cssClass = dyNode.getAttribute("cssClass") as String

        val nodeList = node.childNodes.asList()
        val rowList = nodeList.filter { it.nodeName.equals("bs3:row") }
        for (n: Node in rowList) {
            val row = Row(n)
            rows.add(row)
        }
    }
}
