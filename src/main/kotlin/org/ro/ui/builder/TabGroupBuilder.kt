package org.ro.ui.builder

import org.ro.to.TObject
import org.ro.to.bs3.TabGroup
import org.ro.ui.kv.RoDisplay
import pl.treksoft.kvision.core.Component
import pl.treksoft.kvision.core.CssSize
import pl.treksoft.kvision.core.UNIT
import pl.treksoft.kvision.panel.TabPanel

class TabGroupBuilder {

    fun create(tabGroupLayout: TabGroup, tObject: TObject, dsp: RoDisplay): Component {
        val result = TabPanel()
        result.width = CssSize(100, UNIT.perc)
        result.height = CssSize(100, UNIT.perc)

        for (t in tabGroupLayout.tabList) {
            val cpt = TabBuilder().create(t, tObject, dsp)
            result.addTab(t.name, cpt)
        }
        return result
    }

}
