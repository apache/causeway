package org.ro.layout

import kotlinx.serialization.Serializable
import org.ro.to.bs3.TabGroup

@Serializable
data class TabGroupLayout(val cssClass: String? = "",
                          val metadataError: String? = "",
                          val tab: MutableList<TabLayout> = mutableListOf<TabLayout>(),
                          val collapseIfOne: Boolean? = false,
                          val unreferencedCollections: Boolean? = false
) {
    constructor(tabGroup: TabGroup) : this() {
        tabGroup.tabs.forEach {
            tab.add(TabLayout(it))
        }
    }

}
