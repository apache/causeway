package org.ro.layout

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable
import org.ro.core.TransferObject
import org.ro.view.uicomp.VBox

/**
 * Parse layout specification.
 * In case of non-menu layout, build a UIComponent.
 */
@Serializable
data class Layout(val cssClass: String? = null,
                  val row: List<RowLayout> = emptyList()) : TransferObject {

    @Optional
    var properties = listOf<PropertyLayout>()

    init {
        val row1 = row[1]
        var cols = row1.cols[0]
        var col = cols.col
        if (col != null) {
            val tabGroup = col.tabGroup
            if (tabGroup.size > 0) {
                val tabGroup0 = tabGroup[0]
                val tab0 = tabGroup0.tab[0]
                val row0 = tab0.row[0]
                cols = row0.cols[0]
            }
        }
        col = cols.col!!
        val fieldSet0 = col.fieldSet[0]
        properties = fieldSet0.property
    }

    fun build(): VBox {
        val result = VBox("Layout")
        var b: VBox
        for (rl in row) {
            // row[0] (head) contains the object title and actions (for wicket viewer)
            // this is to be handled differently (tab)
            b = rl.build()
            result.addChild(b)
        }
        return result
    }

}
