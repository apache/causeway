package org.ro.to.bs3

import org.w3c.dom.Node
import org.w3c.dom.asList

class TabGroup(node: Node) {
    var tabs = mutableListOf<Tab>()
    lateinit var metadataError: String
    //@XmlAttribute(name = "collapseIfOne")
    var isCollapseIfOne: Boolean = false
    //@XmlAttribute(name = "unreferencedCollections")
    var isUnreferencedCollections: Boolean = false
    lateinit var cssClass: String

    init {

        val nodeList = node.childNodes.asList()
        val tabNodes = nodeList.filter { it.nodeName.equals("bs3:tab") }
        for (n: Node in tabNodes) {
            val tab = Tab(n)
            tabs.add(tab)
        }

    }
}
