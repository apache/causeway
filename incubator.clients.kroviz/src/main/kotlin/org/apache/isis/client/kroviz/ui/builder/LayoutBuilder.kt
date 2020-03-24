package org.apache.isis.client.kroviz.ui.builder

import org.apache.isis.client.kroviz.layout.Layout
import org.apache.isis.client.kroviz.to.TObject
import org.apache.isis.client.kroviz.to.bs3.Grid
import org.apache.isis.client.kroviz.ui.kv.RoDisplay
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

