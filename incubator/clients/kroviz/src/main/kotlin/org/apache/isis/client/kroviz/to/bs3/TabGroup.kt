package org.apache.isis.client.kroviz.to.bs3

import org.w3c.dom.Node
import org.w3c.dom.asList

class TabGroup(node: Node) {
    var tabList = mutableListOf<Tab>()
    lateinit var metadataError: String
    lateinit var cssClass: String

    init {
        val nodeList = node.childNodes.asList()

        val tnList = nodeList.filter { it.nodeName.equals("bs3:tab") }
        for (n: Node in tnList) {
            val tab =Tab(n)
            tabList.add(tab)
        }
    }

    fun getPropertyList(): List<Property> {
        val list = mutableListOf<Property>()
        tabList.forEach { t ->
            list.addAll(t.getPropertyList())
        }
        return list
    }

}
