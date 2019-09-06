package org.ro.to.bs3

import org.ro.to.bs3.BookmarkPolicy
import org.ro.to.bs3.PromptStyle

/*
@XmlType(name = "action", namespace = "http://isis.apache.org/applib/layout/component", propOrder = {
    "named",
    "describedAs",
    "metadataError",
    "link"
}) */
data class Action(
        val named: String? = null,
        val describedAs: String? = null,
        val metadataError: String? = null,
        val link: Link? = null,
        val bookmarking: BookmarkPolicy? = null,
        val cssClass: String? = null,
        val cssClassFa: String? = null,
        val cssClassFaPosition: String? = null,
        val hidden: Where? = null,
//    @XmlAttribute(name = "id", required = true)
        val id: String,
        val isNamedEscaped: Boolean? = null,
        val position: Position? = null,
        var promptStyle: PromptStyle? = null
) {

}
