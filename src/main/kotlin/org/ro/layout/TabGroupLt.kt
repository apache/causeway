package org.ro.layout

import kotlinx.serialization.Serializable

@Serializable
data class TabGroupLt(val cssClass: String? = "",
                      val metadataError: String? = "",
                      val tab: List<TabLt> = emptyList(),
                      val collapseIfOne: Boolean? = false,
                      val unreferencedCollections: Boolean? = false
)
