package org.ro.ui.builder

import org.ro.layout.Tab
import org.ro.to.TObject
import org.ro.ui.kv.RoDisplay
import pl.treksoft.kvision.core.Component
import pl.treksoft.kvision.core.CssSize
import pl.treksoft.kvision.core.UNIT
import pl.treksoft.kvision.panel.VPanel

class TabBuilder {

    fun create(tabLayout: Tab, tObject: TObject, tab: RoDisplay): Component {
        val result = VPanel()
        result.width = CssSize(100, UNIT.perc)
        result.height = CssSize(100, UNIT.perc)
        var b: VPanel
        for (rl in tabLayout.row) {
            b = RowBuilder().create(rl, tObject, tab)
            b.title = rl.id
            result.add(b)
        }
        return result
    }

}
