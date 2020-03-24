package org.apache.isis.client.kroviz.to.bs3

import org.w3c.dom.Node
import org.w3c.dom.asList

class Tab(node: Node) {
    val rowList = mutableListOf<Row>()
    var name: String

    init {
        val dyNode = node.asDynamic()
        name = dyNode.getAttribute("name") as String

        val nl = node.childNodes.asList()

        val rNodes = nl.filter { it.nodeName.equals("bs3:row") }
        for (n: Node in rNodes) {
            val row =org.apache.isis.client.kroviz.to.bs3.Row(n)
            rowList.add(row)
        }
    }

    fun getPropertyList(): List<Property> {
        val list = mutableListOf<Property>()
        rowList.forEach { r ->
            list.addAll(r.getPropertyList())
        }
        return list
    }

}
