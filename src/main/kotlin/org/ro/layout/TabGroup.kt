package org.ro.layout

import kotlinx.serialization.Serializable

@Serializable
data class TabGroup(val cssClass: String? = "",
                    val metadataError: String? = "",
                    val tab: List<Tab> = emptyList(),
                    val collapseIfOne: Boolean? = false,
                    val unreferencedCollections: Boolean? = false
)
