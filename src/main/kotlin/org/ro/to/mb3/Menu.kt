package org.ro.to.mb3

import org.ro.to.XmlHelper
import org.w3c.dom.Node

class Menu(node: Node) {
    var named: String
    var section: Section

    init {
        val menuList = XmlHelper().nonTextChildren(node)
        val menuNodeList = menuList.filter { !it.toString().contains("unreferencedAction") }
        val menuNode = menuNodeList.first()

        val namedRaw = XmlHelper().firstChildMatching(menuNode, "named")
        named = namedRaw.textContent!!.trim()

        val sectionRaw = XmlHelper().firstChildMatching(menuNode, "section")
        section = Section(sectionRaw)
    }
}
