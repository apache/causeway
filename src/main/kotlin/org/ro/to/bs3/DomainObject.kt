package org.ro.to.bs3

class DomainObject(
        var named: String,
        var describedAs: String,
        var plural: String,
        var metadataError: String,
        var link: Link,
//    @XmlAttribute(name = "bookmarking")
        var bookmarking: BookmarkPolicy,
        var cssClass: String,
        var cssClassFa: String,
        var cssClassFaPosition: CssClassFaPosition,
//    @XmlAttribute(name = "namedEscaped")
        var isNamedEscaped: Boolean) : Bs3Object
