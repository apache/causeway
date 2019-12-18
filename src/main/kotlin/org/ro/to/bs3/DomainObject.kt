package org.ro.to.bs3

import org.ro.to.mb3.XmlHelper
import org.w3c.dom.Node

class DomainObject(node: Node) {
    var named: String
    var plural: String
    lateinit var describedAs: String
    lateinit var metadataError: String
    lateinit var link: Link
    //    @XmlAttribute(name = "bookmarking")
    lateinit var bookmarking: BookmarkPolicy
    lateinit var cssClass: String
    lateinit var cssClassFa: String
    lateinit var cssClassFaPosition: CssClassFaPosition
    //    @XmlAttribute(name = "namedEscaped")
    var isNamedEscaped: Boolean = false

    init {
        val namedNode = XmlHelper().firstChildMatching(node, "named")
        named = namedNode.textContent!!.trim()

        val pluralNode = XmlHelper().firstChildMatching(node, "plural")
        plural = pluralNode.textContent!!.trim()
    }
}
