package org.ro.to.bs3

import org.ro.utils.XmlHelper
import org.w3c.dom.Node

class Row(node: Node) {
    val colList = mutableListOf<Col>()
    var fieldSet:FieldSet? = null
    var tabGroupList = mutableListOf<TabGroup>()
    lateinit var id:String

    init {
        val nodeList = XmlHelper.nonTextChildren(node)
        val cl = nodeList.filter { it.nodeName.equals("bs3:col") }
        for (n: Node in cl) {
            val col = Col(n)
            colList.add(col)
        }

        val fsList = nodeList.filter { it.nodeName.equals("cpt:fieldSet") }
        if (fsList.size == 1) {
            val n = fsList.get(0)
            fieldSet = FieldSet(n)
        }

        val tgList = nodeList.filter { it.nodeName.equals("cpt:fieldSet") }
        for (n: Node in tgList) {
            val tg = TabGroup(n)
            tabGroupList.add(tg)
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
