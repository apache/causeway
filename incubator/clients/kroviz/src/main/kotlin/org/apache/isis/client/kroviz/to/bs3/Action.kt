package org.apache.isis.client.kroviz.to.bs3

import org.w3c.dom.Node
import org.w3c.dom.asList

class Action(node: Node) {
    var bookmarking: String //BookmarkPolicy? = null use ENUM
    var cssClass: String
    var cssClassFa: String
    var cssClassFaPosition: String
    var hidden: String // USE ENUM Where? = null
    var id: String
    var position: String //USE ENUM Position? = null
    var named = ""
    var describedAs = ""

    init {
        val dyNode = node.asDynamic()
        bookmarking = dyNode.getAttribute("bookmarking")
        cssClass = dyNode.getAttribute("cssClass")
        cssClassFa = dyNode.getAttribute("cssClassFa")
        cssClassFaPosition = dyNode.getAttribute("cssClassFaPosition")
        hidden = dyNode.getAttribute("hidden")
        id = dyNode.getAttribute("id")
        position = dyNode.getAttribute("position")
        named = dyNode.getAttribute("named")
        describedAs = dyNode.getAttribute("describedAs")

        val nodeList = node.childNodes.asList()
        val namedList = nodeList.filter { it.nodeName.equals("cpt:named") }
        if (namedList.isNotEmpty()) {
            val n = namedList.first()
            named = n.textContent as String
        }
        val describedAsList = nodeList.filter { it.nodeName.equals("cpt:describedAs") }
        if (describedAsList.isNotEmpty()) {
            val n = describedAsList.first()
            describedAs = n.textContent as String
        }
    }
}
