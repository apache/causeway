package org.ro.to.mb3

import org.ro.org.ro.to.mb3.XmlHelper
import org.w3c.dom.Node

class Section(node: Node) {
    val serviceActions = mutableListOf<ServiceAction>()

    init {
        val nodeList = XmlHelper().nonTextChildren(node)
        for (rawNode: Node in nodeList) {
            val serviceAction = ServiceAction(rawNode)
            serviceActions.add(serviceAction)
        }
    }

}
