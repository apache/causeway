package org.ro.to.bs3

import org.w3c.dom.Node
import org.w3c.dom.asList

class Col(node: Node) {
    var domainObject: DomainObject? = null
    val tabGroupList = mutableListOf<TabGroup>()
    var fieldSetList = mutableListOf<FieldSet>()
    var span: Int = 0

    init {
        val dyNode = node.asDynamic()
        span = dyNode.getAttribute("span")

        val nl = node.childNodes.asList()
        val doNodes = nl.filter { it.nodeName.equals("cpt:domainObject") }
        if (!doNodes.isEmpty()) {
            domainObject = DomainObject(doNodes.first())
        }

        val tgNl = nl.filter { it.nodeName.equals("bs3:tabGroup") }
        for (n: Node in tgNl) {
            val tg = TabGroup(n)
            tabGroupList.add(tg)
        }

        val fsNl = nl.filter { it.nodeName.equals("cpt:fieldSet") }
        for (n in fsNl) {
            val fs = FieldSet(n)
            fieldSetList.add(fs)
        }
    }

    fun getPropertyList(): List<Property> {
        val list = mutableListOf<Property>()
        fieldSetList.forEach { fs ->
            list.addAll(fs.propertyList)
        }
        tabGroupList.forEach { tg ->
            list.addAll(tg.getPropertyList())
        }
        return list
    }

}
