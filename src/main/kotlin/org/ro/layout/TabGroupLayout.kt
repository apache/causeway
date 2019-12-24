package org.ro.layout

import kotlinx.serialization.Serializable
import org.ro.to.bs3.TabGroup
import pl.treksoft.kvision.core.Component
import pl.treksoft.kvision.core.CssSize
import pl.treksoft.kvision.core.UNIT
import pl.treksoft.kvision.panel.TabPanel

@Serializable
data class TabGroupLayout(val cssClass: String? = "",
                          val metadataError: String? = "",
                          val tab: MutableList<TabLayout> = mutableListOf<TabLayout>(),
                          val unreferencedCollections: Boolean? = false
) {
    constructor(tabGroup: TabGroup) : this() {
        tabGroup.tabs.forEach {
            tab.add(TabLayout(it))
        }
    }

    fun build(): Component {
        val result = TabPanel()
        result.width = CssSize(100, UNIT.perc)
        result.height = CssSize(100, UNIT.perc)

        var b: Component
        for (tl in tab) {
            b = tl.build()
            result.add(b)
        }
        return result
    }

}
