package org.ro.bs3.to

// all @XmlElement(required = true)
//TODO is this the same as to org.ro.to.Link ?
data class Link(
        val rel: String,
        val method: String,
        val href: String,
        val type: String)   : Bs3Object
