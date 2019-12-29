package org.ro.layout

import kotlinx.serialization.Serializable
import org.ro.to.Action
import org.ro.to.Member
import org.ro.to.TObject
import org.ro.to.TransferObject
import org.ro.to.bs3.Grid
import pl.treksoft.kvision.panel.VPanel

/**
 * Parse layout specification.
 * Build UI Component tree.
 *
 * @See: https://en.wikipedia.org/wiki/Composite_pattern
 * //TODO eventually use Decorator
 */
@Serializable
data class Layout(val cssClass: String? = null,
                  val row: MutableList<RowLayout> = mutableListOf<RowLayout>()) : TransferObject {

    var properties = listOf<PropertyLayout>()
    var actions: Map<String, Action>? = null

    constructor(grid: Grid) : this() {
        grid.rows.forEach {
            row.add(RowLayout(it))
        }
    }

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

    fun build(tObject: TObject, members : Map<String, Member>): VPanel {
        val result = VPanel()
        val rlt = row[0]
        val oCpt = rlt.build(tObject, actions)
        result.add(oCpt)

        for (rl in row) {
            // row[0] (head) contains the object title and actions (for wicket viewer)
            // this is to be handled differently (tab)
            val cpt = rl.build(members)
            result.add(cpt)
        }
        return result
    }

}
