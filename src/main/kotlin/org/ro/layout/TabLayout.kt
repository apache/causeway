package org.ro.layout

import kotlinx.serialization.Serializable
import org.ro.to.bs3.Tab
import pl.treksoft.kvision.core.Component
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
        val result = VPanel()
        result.title = name
        var b: VPanel
        for (rl in row) {
            b = rl.build()
            b.title = rl.id
            result.add(b)
        }
        return result
    }

}
