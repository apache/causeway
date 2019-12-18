package org.ro.to.bs3

import org.ro.to.mb3.XmlHelper
import org.w3c.dom.Node

class Row(node: Node) {
    var cols = ArrayList<Col>()
    var fieldSet:FieldSet? = null

    init {
        val nodeList = XmlHelper().nonTextChildren(node)
        val colList = nodeList.filter { it.nodeName.equals("bs3:col") }
        for (n: Node in colList) {
            val col = Col(n)
            cols.add(col)
        }

        val fieldSetList = nodeList.filter { it.nodeName.equals("cpt:fieldSet") }
        if (fieldSetList.size == 1) {
            val n = fieldSetList.get(0)
            fieldSet = FieldSet(n)
        }
    }
}
