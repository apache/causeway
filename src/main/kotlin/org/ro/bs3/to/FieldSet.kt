package org.ro.bs3.to

class FieldSet(
        protected var action: List<Action>? = ArrayList(),
        protected var property: List<Property>? = ArrayList<Property>(),
        var metadataError: String,
        var name: String,
        var id: String,
//   @XmlAttribute(name = "unreferencedActions")
        var isUnreferencedActions: Boolean,
//    @XmlAttribute(name = "unreferencedProperties")
        var isUnreferencedProperties: Boolean)
