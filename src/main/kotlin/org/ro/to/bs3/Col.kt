package org.ro.to.bs3

//    @XmlElement(namespace = "http://isis.apache.org/applib/layout/component")
data class Col(
        protected var sizeSpan: List<SizeSpan>? = ArrayList<SizeSpan>(),
        var domainObject: DomainObject,
        protected var action: List<Action>? = ArrayList(),
        protected var row: List<Row>? = ArrayList<Row>(),
        protected var tabGroup: List<TabGroup>? = ArrayList<TabGroup>(),
        protected var fieldSet: List<FieldSet>? = ArrayList<FieldSet>(),
        protected var collection: List<Collection>? = ArrayList<Collection>(),
        var metadataError: String,
        var id: String,
        // @XmlAttribute(name = "span", required = true)
        var span: Int = 0,
        // @XmlAttribute(name = "unreferencedActions")
        var isUnreferencedActions: Boolean,
        // @XmlAttribute(name = "unreferencedCollections")
        var isUnreferencedCollections: Boolean,
        override var size: Size,
        override var cssClass: String
) : Bs3RowContent(size, cssClass)
