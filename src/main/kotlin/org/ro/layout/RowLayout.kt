package org.ro.layout

import kotlinx.serialization.Serializable
import org.ro.to.bs3.Row
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

    fun build(): VPanel {
        val result = VPanel()
        var b: VPanel
        for (c in cols) {
            b = c.build()
            result.add(b)
        }
        return result
    }

}
