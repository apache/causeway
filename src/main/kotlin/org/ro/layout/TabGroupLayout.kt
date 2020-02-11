package org.ro.layout

import kotlinx.serialization.Serializable

@Serializable
data class TabGroupLayout(val cssClass: String? = "",
                          val metadataError: String? = "",
                          val tab: MutableList<TabLayout> = mutableListOf<TabLayout>(),
                          val collapseIfOne: Boolean? = false,
                          val unreferencedCollections: Boolean? = false
)
