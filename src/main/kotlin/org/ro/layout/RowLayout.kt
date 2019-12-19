package org.ro.layout

import kotlinx.serialization.Serializable
import org.ro.to.bs3.Row
import org.ro.ui.uicomp.Box
import org.ro.ui.uicomp.VBox

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

    fun build(): VBox {
        val result = VBox("RowLayout")
        var b: Box
        for (c in cols) {
            b = c.build()
            result.addChild(b)
        }
        return result
    }

}
