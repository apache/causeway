package org.ro.layout

import kotlinx.serialization.Serializable
import org.ro.to.Link

@Serializable
data class ActionLt(var named: String? = "",
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
)
