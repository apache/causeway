package org.ro.to.bs3

import org.ro.to.Link
import org.ro.utils.XmlHelper
import org.w3c.dom.Node

class DomainObject(node: Node) {
    var named = ""
    var plural = ""
    lateinit var describedAs: String
    lateinit var metadataError: String
    lateinit var link: Link
    lateinit var cssClass: String
    lateinit var cssClassFa: String

    init {
        val nn = XmlHelper.firstChildMatching(node, "named")
        if (nn?.textContent != null) {
            named = nn.textContent!!.trim()
        }

        val pn = XmlHelper.firstChildMatching(node, "plural")
        if (pn?.textContent != null) {
            plural = pn.textContent!!.trim()
        }
    }

}
