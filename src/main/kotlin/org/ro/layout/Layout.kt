package org.ro.layout

import kotlinx.serialization.ContextualSerialization
import kotlinx.serialization.Serializable
import org.ro.to.TransferObject
import org.ro.to.bs3.Grid

/**
 * Wraps Grid and adds (marker) interface TransferObject
 *
 * @See: https://en.wikipedia.org/wiki/Composite_pattern
 */
@Serializable
data class Layout(val cssClass: String? = null,
                  val row: List<RowLt> = emptyList()) : TransferObject {
    val propertyList = mutableListOf<PropertyLt>()
    val propertyDescriptionList = mutableListOf<org.ro.to.Property>()
    @ContextualSerialization
    var grid: Grid? = null

    fun initGrid(grid:Grid) {
        //propertyList.addAll(grid.getPropertyList())
    }

    fun addPropertyDescription(p: org.ro.to.Property) {
        propertyDescriptionList.add(p)
    }

    init {
        // row[0] (head) contains the object title and actions
        // row[1] contains data, tabs, collections, etc.
        val secondRow = row[1] // traditional C braintwist
        var colsLyt = secondRow.cols.first()
        var colLyt = colsLyt.getCol()
        val tgLyts = colLyt.tabGroup
        if (tgLyts.isNotEmpty()) {
            val tabGroup = tgLyts.first()
            val tab = tabGroup.tab.first()
            val row = tab.row.first()
            colsLyt = row.cols.first()
        }
        colLyt = colsLyt.getCol()
        val fsList = colLyt.fieldSet
        if (fsList.isNotEmpty()) {
            val fsLyt = fsList.first()
            //propertyList = fsLyt.property
        }
    }

}
