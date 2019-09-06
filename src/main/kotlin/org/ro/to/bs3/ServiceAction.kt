package org.ro.to.bs3

class ServiceAction(
        var named: String,
        var describedAs: String,
        var metadataError: String,
        var link: Link,
        //@XmlAttribute(name = "objectType", required = true)
        var objectType: String,
        //@XmlAttribute(name = "id", required = true)
        var id: String,
        //@XmlAttribute(name = "namedEscaped")
        var isNamedEscaped: Boolean,
        //@XmlAttribute(name = "bookmarking")
        var bookmarking: BookmarkPolicy,
        //@XmlAttribute(name = "cssClass")
        var cssClass: String,
        var cssClassFa: String
) {

}
