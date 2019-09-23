package org.ro.org.ro.bs3.parser

import org.ro.bs3.parser.BaseXmlHandler
import org.ro.bs3.to.BookmarkPolicy
import org.ro.bs3.to.Bs3Object
import org.ro.bs3.to.CssClassFaPosition
import org.ro.bs3.to.DomainObject

class DomainObjectHandler : BaseXmlHandler() {
    override fun doHandle() {
//        logEntry.aggregator = NavigationAggregator()
        //      update()
    }

    override fun parse(xmlStr: String): Bs3Object? {
        //TODO dive into sub elements, create objects and use in constructor
        var named = ""
        var describedAs = ""
        var plural = ""
        var metadataError = ""
        var link = LinkHandler(xmlString)
//    @XmlAttribute(name = "bookmarking")
        var bookmarking = BookmarkPolicy.AS_CHILD
        var cssClass = ""
        var cssClassFa = ""
        var cssClassFaPosition = CssClassFaPosition.LEFT
//    @XmlAttribute(name = "namedEscaped")
        var isNamedEscaped = false
        return DomainObject(
                named,
                describedAs,
                plural,
                metadataError,
                link,
                bookmarking,
                cssClass,
                cssClassFa,
                cssClassFaPosition,
                isNamedEscaped)
    }
}
