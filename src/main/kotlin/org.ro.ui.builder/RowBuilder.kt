package org.ro.ui.builder

import org.ro.layout.RowLayout
import org.ro.to.TObject
import org.ro.ui.kv.MenuFactory
import org.ro.ui.kv.RoDisplay
import pl.treksoft.kvision.core.CssSize
import pl.treksoft.kvision.core.UNIT
import pl.treksoft.kvision.panel.HPanel
import pl.treksoft.kvision.panel.VPanel

class RowBuilder {

    fun create(rowLayout: RowLayout, tObject: TObject, dsp: RoDisplay): VPanel {
        val result = VPanel()
        result.width = CssSize(100, UNIT.perc)
        result.height = CssSize(100, UNIT.perc)

        for (c in rowLayout.cols) {
            val cpt = ColsBuilder().create(c, tObject, dsp)
            result.add(cpt)
        }
        return result
    }

    fun createMenu(tObject: TObject, dsp: RoDisplay): HPanel {
        val result = HPanel()
        result.width = CssSize(100, UNIT.perc)
        result.height = CssSize(100, UNIT.perc)

        val dd = MenuFactory.buildFor(tObject)
        dd.marginTop = CssSize(10, UNIT.px)
        dd.marginBottom = CssSize(10, UNIT.px)
        MenuFactory.amendWithSaveUndo(dd, tObject)
        MenuFactory.disableSaveUndo(dd)
        dsp.menu = dd
        result.add(dd)

        return result
    }



}
