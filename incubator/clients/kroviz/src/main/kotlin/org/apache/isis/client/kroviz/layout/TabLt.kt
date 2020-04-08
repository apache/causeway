package org.apache.isis.client.kroviz.layout

import kotlinx.serialization.Serializable

@Serializable
data class TabLt(val cssClass: String? = null,
                 val name: String? = null,
                 val row: List<RowLt>
)
