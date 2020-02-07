package org.ro.to

import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.asList
import org.w3c.dom.parsing.DOMParser

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

    fun parseXml(xmlStr: String): Document {
        val p = DOMParser()
        return p.parseFromString(xmlStr, "application/xml")
    }

}
