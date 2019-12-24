package org.ro.layout

import kotlinx.serialization.Serializable
import org.ro.to.bs3.Tab
import pl.treksoft.kvision.core.Component
import pl.treksoft.kvision.core.CssSize
import pl.treksoft.kvision.core.UNIT
import pl.treksoft.kvision.panel.TabPanel
import pl.treksoft.kvision.panel.VPanel

@Serializable
data class TabLayout(val cssClass: String? = null,
                     val name: String? = null,
                     val row: MutableList<RowLayout> = mutableListOf<RowLayout>()
) {
    constructor(tab: Tab) : this() {
        tab.rows.forEach {
            row.add(RowLayout(it))
        }
    }

    fun build(): Component {
        val result = TabPanel()
        result.width = CssSize(100, UNIT.perc)
        result.height = CssSize(100, UNIT.perc)

        var b: VPanel
        for (rl in row) {
            b = rl.build()
            result.add(b)
        }
        return result
    }

}
