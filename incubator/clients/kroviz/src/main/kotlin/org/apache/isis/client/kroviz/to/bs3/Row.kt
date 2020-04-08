package org.apache.isis.client.kroviz.to.bs3

import org.apache.isis.client.kroviz.utils.XmlHelper
import org.w3c.dom.Node

class Row(node: Node) {
    val colList = mutableListOf<Col>()
    var id: String = ""

    init {
        val dyNode = node.asDynamic()
        if (dyNode.hasOwnProperty("id")) {
            id = dyNode.getAttribute("id") as String
        }

        val nodeList = XmlHelper.nonTextChildren(node)
        val cl = nodeList.filter { it.nodeName.equals("bs3:col") }
        for (n: Node in cl) {
            val col = Col(n)
            colList.add(col)
        }
    }

    fun getPropertyList(): List<Property> {
        val list = mutableListOf<Property>()
        colList.forEach { c ->
            list.addAll(c.getPropertyList())
        }
        return list
    }

}
