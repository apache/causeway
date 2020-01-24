package org.ro.ui.builder

import org.ro.layout.TabGroupLayout
import org.ro.to.TObject
import org.ro.ui.kv.RoDisplay
import pl.treksoft.kvision.core.Component
import pl.treksoft.kvision.core.CssSize
import pl.treksoft.kvision.core.UNIT
import pl.treksoft.kvision.panel.TabPanel

class TabGroupBuilder {

    fun create(tabGroupLayout: TabGroupLayout, tObject: TObject, dsp: RoDisplay): Component {
        val result = TabPanel()
        result.width = CssSize(100, UNIT.perc)
        result.height = CssSize(100, UNIT.perc)

        for (tl in tabGroupLayout.tab) {
            val cpt = TabBuilder().create(tl, tObject, dsp)
            result.addTab(tl.name!!, cpt)
        }
        return result
    }

}
