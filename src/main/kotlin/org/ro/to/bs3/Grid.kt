package org.ro.to.bs3

class Grid(
//    @XmlElement(required = true)
        protected var row: List<Row>? = ArrayList<Row>(),
        protected var metadataError: List<String>? = ArrayList(),
//    @XmlAttribute(name = "cssClass")
        var cssClass: String

) {

}
