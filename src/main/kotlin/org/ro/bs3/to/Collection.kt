package org.ro.to.bs3

data class Collection(
        var named: String,
        var describedAs: String,
        var sortedBy: String,
        protected var action: List<Action>? = ArrayList(),
        var metadataError: String,
        var link: Link,
        var cssClass: String,
        var defaultView: String,
        var hidden: Where,
//@XmlAttribute(name = "id", required = true)
        var id: String,
//@XmlAttribute(name = "namedEscaped")
        var isNamedEscaped: Boolean,
        var paged: Int)
