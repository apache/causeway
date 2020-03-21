package org.apache.isis.client.kroviz.to.bs3

import org.apache.isis.client.kroviz.to.Link
import org.w3c.dom.Node
import org.w3c.dom.asList

//IMPROVE class differs in many aspects from org.ro.to.Property - to be refactored?
class Property(node: Node) {
    var id: String
    var link:org.apache.isis.client.kroviz.to.Link? = null
    var hidden: String // USE ENUM Where? = null
    var typicalLength: Int = 0
    var multiLine: Int = 1
    var describedAs: String? = null
    var named = ""
    lateinit var action:org.apache.isis.client.kroviz.to.bs3.Action

    init {
        val dn = node.asDynamic()
        hidden = dn.getAttribute("hidden")
        id = dn.getAttribute("id") as String
        typicalLength = dn.getAttribute("typicalLength")
        multiLine = dn.getAttribute("multiLine")
        describedAs = dn.getAttribute("describedAs")

        val nodeList = node.childNodes.asList()
        val namedList = nodeList.filter { it.nodeName.equals("cpt:named") }
        if (namedList.isNotEmpty()) {
            val n = namedList.first()
            named = n.textContent as String
        }

        val actList = nodeList.filter { it.nodeName.equals("cpt:action") }
        if (actList.isNotEmpty()) {
            val n = actList.first()
            action =org.apache.isis.client.kroviz.to.bs3.Action(n)
        }

        val linkList = nodeList.filter { it.nodeName.equals("cpt:link") }
        if (linkList.isNotEmpty()) {
            val n = linkList.first()
            val bs3l =org.apache.isis.client.kroviz.to.bs3.Link(n)
            link =org.apache.isis.client.kroviz.to.Link(bs3l.rel, bs3l.method, bs3l.href, bs3l.type)
        }
    }

}
