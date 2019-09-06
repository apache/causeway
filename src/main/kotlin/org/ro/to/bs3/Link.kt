package org.ro.to.bs3

// all @XmlElement(required = true)
data class Link(
        val rel: String,
        val method: String,
        val href: String,
        val type: String) {
}
