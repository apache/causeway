package org.ro.ui.builder

import org.ro.layout.Layout
import org.ro.to.TObject
import org.ro.to.bs3.Grid
import org.ro.ui.kv.RoDisplay
import pl.treksoft.kvision.core.CssSize
import pl.treksoft.kvision.core.UNIT
import pl.treksoft.kvision.panel.VPanel

class LayoutBuilder {

    fun create(layout: Layout, grid: Grid, tObject: TObject, dsp: RoDisplay): VPanel {
        val result = VPanel()

        val oCpt = RowBuilder().createMenu(tObject, dsp)
        oCpt.width = CssSize(100, UNIT.perc)
        result.add(oCpt)

        for (rl in grid.rows) {
            val cpt = RowBuilder().create(rl, tObject, dsp)
            result.add(cpt)
        }
        return result
    }

}

