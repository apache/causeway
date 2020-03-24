package org.apache.isis.client.kroviz.to.bs3

import org.w3c.dom.Node
import org.w3c.dom.asList

class FieldSet(node: Node) {
    var actionList = mutableListOf<Action>()
    var propertyList = mutableListOf<Property>()
    var name: String
    var id: String

    init {
        val dyNode = node.asDynamic()
        name = dyNode.getAttribute("name") as String
        id = dyNode.getAttribute("id") as String

        val nl = node.childNodes.asList()
        val actList = nl.filter { it.nodeName.equals("cpt:action") }
        for (n: Node in actList) {
            val act = Action(n)
            actionList.add(act)
        }

        val pNl = nl.filter { it.nodeName.equals("cpt:property") }
        for (n: Node in pNl) {
            val p = Property(n)
            propertyList.add(p)
        }
    }

}
