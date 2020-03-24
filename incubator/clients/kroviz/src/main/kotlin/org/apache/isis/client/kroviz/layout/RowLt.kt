package org.apache.isis.client.kroviz.layout

import kotlinx.serialization.Serializable

@Serializable
data class RowLt(val cols: List<ColsLt> = emptyList(),
                 val metadataError: String? = null,
                 val cssClass: String? = null,
                 val id: String? = null
)

