package org.apache.isis.client.kroviz.utils

import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.asList
import org.w3c.dom.parsing.DOMParser

object XmlHelper {

    fun isXml(input: String): Boolean {
        val s = input.trim()
        return s.startsWith("<") && s.endsWith(">")
    }

    fun nonTextChildren(node: Node): List<Node> {
        val match = "#text"
        val childNodes = node.childNodes.asList()
        return childNodes.filter { !it.nodeName.contains(match) }
    }

    fun firstChildMatching(node: Node, match: String): Node? {
        val childNodes = node.childNodes.asList()
        val list = childNodes.filter { it.nodeName.contains(match) }
        return list.firstOrNull()
    }

    fun parseXml(xmlStr: String): Document {
        val p = DOMParser()
        return p.parseFromString(xmlStr, "application/xml")
    }

}
