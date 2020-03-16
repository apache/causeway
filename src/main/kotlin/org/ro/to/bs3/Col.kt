package org.ro.to.bs3

import org.w3c.dom.Node
import org.w3c.dom.asList

class Col(node: Node) {
    var domainObject: DomainObject? = null
    var actionList = mutableListOf<Action>()
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

        val actNodes = nl.filter { it.nodeName.equals("cpt:action") }
        for (n: Node in actNodes) {
            val act = Action(n)
            actionList.add(act)
        }

        val tgNodes = nl.filter { it.nodeName.equals("bs3:tabGroup") }
        for (n: Node in tgNodes) {
            val tg = TabGroup(n)
            tabGroupList.add(tg)
        }

        val fsNodes = nl.filter { it.nodeName.equals("cpt:fieldSet") }
        for (n: Node in fsNodes) {
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
