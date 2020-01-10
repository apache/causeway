package org.ro.layout

import kotlinx.serialization.Serializable
import org.ro.to.TObject
import org.ro.to.bs3.TabGroup
import org.ro.ui.kv.RoDisplay
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

    fun build(tObject: TObject, dsp: RoDisplay): Component {
        val result = TabPanel()
        result.width = CssSize(100, UNIT.perc)
        result.height = CssSize(100, UNIT.perc)

        for (tl in tab) {
            val cpt = tl.build(tObject, dsp)
            result.addTab(tl.name!!, cpt)
        }
        return result
    }

}
