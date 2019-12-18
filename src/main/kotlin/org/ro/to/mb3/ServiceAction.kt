package org.ro.to.mb3

import org.ro.to.mb3.XmlHelper
import org.ro.to.bs3.Link
import org.w3c.dom.Node

class ServiceAction(node: Node) {
    var objectType: String
    var id: String
    var named: String
    var link: Link

    init {
        val dyNode = node.asDynamic()
        objectType = dyNode.getAttribute("objectType") as String
        id = dyNode.getAttribute("id") as String

        val namedNode = XmlHelper().firstChildMatching(node, "named")
        named = namedNode.textContent!!.trim()

        val linkNode = XmlHelper().firstChildMatching(node, "link")
        val relNode = XmlHelper().firstChildMatching(linkNode, "lnk:rel")
        val rel = relNode.textContent!!.trim()

        val methodNode = XmlHelper().firstChildMatching(linkNode, "lnk:method")
        val method = methodNode.textContent!!.trim()

        val hrefNode = XmlHelper().firstChildMatching(linkNode, "lnk:href")
        val href = hrefNode.textContent!!.trim()

        val typeNode = XmlHelper().firstChildMatching(linkNode, "lnk:type")
        val type = typeNode.textContent!!.trim()

        link = Link(rel, method, href, type)
    }
}
