package org.ro.to.bs3

import org.w3c.dom.Node
import org.w3c.dom.asList

class Property(node: Node) {
    var hidden: String // USE ENUM Where? = null
    var id: String
    var typicalLength: Int = 0

    var named = ""
    lateinit var action: Action

    init {
        val dyNode = node.asDynamic()
        hidden = dyNode.getAttribute("hidden")
        id = dyNode.getAttribute("id") as String
        typicalLength = dyNode.getAttribute("typicalLength")

        val nodeList = node.childNodes.asList()
        val namedList = nodeList.filter { it.nodeName.equals("cpt:named") }
        if (namedList.isNotEmpty()) {
            val n = namedList.first()
            named = n.textContent as String
        }

        val actList = nodeList.filter { it.nodeName.equals("cpt:action") }
        if (actList.isNotEmpty()) {
            val n = actList.first()
            action = Action(n)
        }
    }

}
