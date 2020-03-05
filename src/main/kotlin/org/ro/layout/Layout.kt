package org.ro.layout

import kotlinx.serialization.Serializable
import org.ro.to.TransferObject

/**
 * Parse layout specification.
 * Build UI Component tree.
 *
 * @See: https://en.wikipedia.org/wiki/Composite_pattern
 */
@Serializable
data class Layout(val cssClass: String? = null,
                  val row: List<Row> = emptyList()) : TransferObject {
    var properties = listOf<Property>()

    init {
        // row[0] (head) contains the object title and actions
        // row[1] contains data, tabs, collections, etc.
        val secondRow = row[1] // traditional C braintwist
        var cols = secondRow.cols.first()
        var col = cols.getCol()
        val tgList = col.tabGroup
        if (tgList.isNotEmpty()) {
            val tabGroup = tgList.first()
            val tab = tabGroup.tab.first()
            val row = tab.row.first()
            cols = row.cols.first()
        }
        col = cols.getCol()
        val fsList = col.fieldSet
        if (fsList.isNotEmpty()) {
            val fs = fsList.first()
            properties = fs.property
        }
    }

}
