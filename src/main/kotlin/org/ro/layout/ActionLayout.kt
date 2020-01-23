package org.ro.layout

import kotlinx.serialization.Serializable
import org.ro.to.Link
import org.ro.to.bs3.Action

@Serializable
data class ActionLayout(var named: String? = "",
                        var describedAs: String? = "",
                        var metadataError: String? = "",
                        var link: Link? = null,
                        var id: String? = "",
                        var bookmarking: String? = "",
                        var cssClass: String? = "",
                        var cssClassFa: String? = "",
                        var cssClassFaPosition: String? = "",
                        var hidden: String? = null,
                        var namedEscaped: String? = "",
                        var position: String? = "",
                        var promptStyle: String? = "",
                        val redirect: String? = null
) {
    constructor(action: Action) : this() {
        named = action.named
        describedAs = action.describedAs
        //TODO link = action.link
        id = action.id
        bookmarking = action.bookmarking
        cssClass = action.cssClass
        cssClassFa = action.cssClassFa
        cssClassFaPosition = action.cssClassFaPosition
        hidden = action.hidden
        position = action.position
    }

}
