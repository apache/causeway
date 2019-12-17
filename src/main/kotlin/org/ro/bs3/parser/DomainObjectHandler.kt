package org.ro.bs3.parser

import org.ro.to.bs3.*


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
        var link = LinkHandler().parse(xmlStr) as Link
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
