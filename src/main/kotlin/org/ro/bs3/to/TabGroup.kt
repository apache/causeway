package org.ro.bs3.to

class TabGroup(
        protected var tab: List<Tab>? = ArrayList<Tab>(),
        var metadataError: String,
        //@XmlAttribute(name = "collapseIfOne")
        var isCollapseIfOne: Boolean,
        //@XmlAttribute(name = "unreferencedCollections")
        var isUnreferencedCollections: Boolean,
        cssClass: String) : Bs3ElementAbstract(cssClass)
