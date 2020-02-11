package org.ro.layout

import kotlinx.serialization.Serializable

@Serializable
data class RowLayout(val cols: MutableList<ColsLayout> = mutableListOf<ColsLayout>(),
                     val metadataError: String? = null,
                     val cssClass: String? = null,
                     val id: String? = null
)
//    private val maxSpan = 12
