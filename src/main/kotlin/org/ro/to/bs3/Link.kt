package org.ro.to.bs3

import org.w3c.dom.Node
import org.w3c.dom.asList

class Link(node: Node) {
    lateinit var rel: String
    lateinit var method: String
    lateinit var href: String
    lateinit var type: String

    init {
        val nodeList = node.childNodes.asList()

        val relList = nodeList.filter { it.nodeName.equals("lnk:rel") }
        if (relList.isNotEmpty()) {
            val n = relList.first()
            rel = n.textContent as String
        }

        val methodList = nodeList.filter { it.nodeName.equals("lnk:method") }
        if (methodList.isNotEmpty()) {
            val n = methodList.first()
            method = n.textContent as String
        }

        val hrefList = nodeList.filter { it.nodeName.equals("lnk:href") }
        if (hrefList.isNotEmpty()) {
            val n = hrefList.first()
            href = n.textContent as String
        }

        val typeList = nodeList.filter { it.nodeName.equals("lnk:type") }
        if (typeList.isNotEmpty()) {
            val n = typeList.first()
            type = n.textContent as String
        }
    }

}
