package org.ro.to.bs3

class Tab(
        //@XmlElement(required = true)
        protected var row: List<Row>? = ArrayList<Row>(),
        //@XmlAttribute(name = "name", required = true)
        var name: String,
        cssClass: String
) : Bs3ElementAbstract(cssClass) {

}
