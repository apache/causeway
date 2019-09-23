package org.ro.bs3.to

class Property(
        var named: String,
        var describedAs: String,
        protected var action: List<Action>? = ArrayList(),
        var metadataError: String,
        var link: Link,
        var cssClass: String,
        var hidden: Where,
        //@XmlAttribute(name = "id", required = true)
        var id: String,
        var labelPosition: LabelPosition,
        var multiLine: Int,
        //@XmlAttribute(name = "namedEscaped")
        var isNamedEscaped: Boolean,
        var promptStyle: PromptStyle,
        //@XmlAttribute(name = "renderedAsDayBefore")
        var isRenderedAsDayBefore: Boolean,
        var typicalLength: Int,
        //@XmlAttribute(name = "unchanging")
        var isUnchanging: Boolean)
