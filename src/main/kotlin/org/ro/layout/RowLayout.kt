package org.ro.layout

import kotlinx.serialization.Serializable
import org.ro.to.Member
import org.ro.to.TObject
import org.ro.to.bs3.Row
import org.ro.ui.kv.MenuFactory
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

    fun build(tObject: TObject): HPanel {
        val result = HPanel()
        result.width = CssSize(100, UNIT.perc)
        result.height = CssSize(100, UNIT.perc)

        val dd = MenuFactory.buildDdFor(tObject)
        dd.marginTop = CssSize(10, UNIT.px)
        dd.marginBottom = CssSize(10, UNIT.px)
        result.add(dd)

        return result
    }

    fun build(members: Map<String, Member>): VPanel {
        val result = VPanel()
        result.width = CssSize(100, UNIT.perc)
        result.height = CssSize(100, UNIT.perc)

        for (c in cols) {
            val cpt = c.build(members)
            result.add(cpt)
        }
        return result
    }

}
