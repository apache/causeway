package org.apache.isis.client.kroviz.layout

import kotlinx.serialization.Serializable
import org.apache.isis.client.kroviz.to.Link

@Serializable
data class CollectionLt(var named: String? = "",
                        var describedAs: String? = "",
                        var sortedBy: String? = "",
                        var action: List<ActionLt> = emptyList(),
                        var metadataError: String? = "",
                        var link:Link? = null,
                        var id: String? = "",
                        var cssClass: String? = "",
                        var defaultView: String? = null,
                        var hidden: String? = null,
                        var namedEscaped: String? = "",
                        var paged: String? = ""
)

