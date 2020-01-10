package org.ro.layout

import kotlinx.serialization.Serializable
import org.ro.to.TObject
import org.ro.to.bs3.Row
import org.ro.ui.kv.MenuFactory
import org.ro.ui.kv.RoDisplay
import pl.treksoft.kvision.core.CssSize
import pl.treksoft.kvision.core.UNIT
import pl.treksoft.kvision.panel.HPanel
import pl.treksoft.kvision.panel.VPanel

@Serializable
data class RowLayout(val cols: MutableList<ColsLayout> = mutableListOf<ColsLayout>(),
                     val metadataError: String? = null,
                     val cssClass: String? = null,
                     val id: String? = null
) {
//    private val maxSpan = 12

    constructor(row: Row) : this() {
        row.cols.forEach {
            cols.add(ColsLayout(it))
        }
    }

    fun buildMenu(tObject: TObject, dsp: RoDisplay): HPanel {
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

    fun build(tObject: TObject, dsp: RoDisplay): VPanel {
        val result = VPanel()
        result.width = CssSize(100, UNIT.perc)
        result.height = CssSize(100, UNIT.perc)

        for (c in cols) {
            val cpt = c.build(tObject, dsp)
            result.add(cpt)
        }
        return result
    }

}
