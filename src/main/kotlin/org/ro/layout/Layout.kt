package org.ro.layout

import kotlinx.serialization.Serializable
import org.ro.to.TransferObject
import org.ro.to.bs3.Grid

/**
 * Parse layout specification.
 * Build UI Component tree.
 *
 * @See: https://en.wikipedia.org/wiki/Composite_pattern
 */
@Serializable
data class Layout(val cssClass: String? = null,
                  val row: MutableList<RowLayout> = mutableListOf<RowLayout>()) : TransferObject {

    var properties = listOf<PropertyLayout>()

    constructor(grid: Grid) : this() {
        grid.rows.forEach {
            row.add(RowLayout(it))
        }
    }

    init {
        // row[0] (head) contains the object title and actions
        // row[1] contains data, tabs, collections, etc.
        val secondRow = row[1] // traditional C braintwist
        var colsLyt = secondRow.cols.first()
        var colLyt = colsLyt.col
        if (colLyt != null) {
            val tgLyts = colLyt.tabGroup
            if (tgLyts.isNotEmpty()) {
                val tabGroup = tgLyts.first()
                val tab = tabGroup.tab.first()
                val row = tab.row.first()
                colsLyt = row.cols.first()
            }
        }
        colLyt = colsLyt.col!!
        val fsLyt = colLyt.fieldSet.first()
        properties = fsLyt.property
    }

}
