package org.apache.isis.client.kroviz.ui.builder

import org.apache.isis.client.kroviz.to.TObject
import org.apache.isis.client.kroviz.to.bs3.Row
import org.apache.isis.client.kroviz.ui.kv.MenuFactory
import org.apache.isis.client.kroviz.ui.kv.RoDisplay
import pl.treksoft.kvision.core.CssSize
import pl.treksoft.kvision.core.UNIT
import pl.treksoft.kvision.panel.*

class RowBuilder {

    fun create(row: Row, tObject: TObject, dsp: RoDisplay): SimplePanel {
        val result = FlexPanel(
                FlexDir.ROW,
                FlexWrap.NOWRAP,
                FlexJustify.FLEXSTART,
                FlexAlignItems.FLEXSTART,
                FlexAlignContent.STRETCH,
                spacing = 10 )

        for (c in row.colList) {
            val cpt = ColBuilder().create(c, tObject, dsp)
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
