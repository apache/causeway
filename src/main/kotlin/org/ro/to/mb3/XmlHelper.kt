package org.ro.to.mb3

import org.w3c.dom.Node
import org.w3c.dom.asList

class XmlHelper {

    fun nonTextChildren(node: Node): List<Node> {
        val match = "#text"
        val childNodes = node.childNodes.asList()
        val list = childNodes.filter { !it.nodeName.contains(match) }
        return list
    }

    fun firstChildMatching(node: Node, match: String): Node {
        val childNodes = node.childNodes.asList()
        val list = childNodes.filter { it.nodeName.contains(match) }
        return list.first()
    }

}
