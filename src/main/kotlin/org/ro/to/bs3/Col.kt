package org.ro.to.bs3

import org.w3c.dom.Node
import org.w3c.dom.asList

class Col(node: Node) {
    var domainObject: DomainObject? = null
    var tabGroups = mutableListOf<TabGroup>()
    var fieldSet:FieldSet? = null
    var span: Int = 0

    init {
        val dyNode = node.asDynamic()
        span = dyNode.getAttribute("span")

        val nodeList = node.childNodes.asList()
        val doNodes = nodeList.filter { it.nodeName.equals("cpt:domainObject") }
        if (!doNodes.isEmpty()) {
            domainObject = DomainObject(doNodes.first())
        }

        val tgNodes = nodeList.filter { it.nodeName.equals("bs3:tabGroup") }
        for (n:Node in tgNodes) {
            val tabGroup = TabGroup(n)
            tabGroups.add(tabGroup)
        }

        val fieldSetList = nodeList.filter { it.nodeName.equals("cpt:fieldSet") }
        if (fieldSetList.size == 1) {
            val n = fieldSetList.get(0)
            fieldSet = FieldSet(n)
        }
    }

}
