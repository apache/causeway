package org.ro.to.bs3

import org.w3c.dom.Node
import org.w3c.dom.asList

class FieldSet(node: Node) {
    var actions = mutableListOf<Action>()
    var properties = mutableListOf<Property>()
    var name: String
    var id: String

    init {
        val dyNode = node.asDynamic()
        name = dyNode.getAttribute("name") as String
        id = dyNode.getAttribute("id") as String

        val nodeList = node.childNodes.asList()
        val actList = nodeList.filter { it.nodeName.equals("cpt:action") }
        for (n: Node in actList) {
            val act = Action(n)
            actions.add(act)
        }

        val propList = nodeList.filter { it.nodeName.equals("cpt:property") }
        for (n: Node in propList) {
            val prop = Property(n)
            properties.add(prop)
        }
    }
}
