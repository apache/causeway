package org.ro.ui.kv

import org.ro.core.model.DisplayList
import org.ro.core.model.Exposer
import pl.treksoft.kvision.panel.SimplePanel
import pl.treksoft.kvision.table.TableType
import pl.treksoft.kvision.tabulator.Layout
import pl.treksoft.kvision.tabulator.Tabulator
import pl.treksoft.kvision.tabulator.TabulatorOptions
import pl.treksoft.kvision.tabulator.tabulator

/**
 * access attributes from dynamic (JS) objects with varying
 * - numbers of attributes
 * - attribute types (can only be determined at runtime) and
 * - accessor names
 */
class RoTable(displayList: DisplayList) : SimplePanel() {

    init {
        title = displayList.extractTitle()
        val model = displayList.data
        val columns = ColumnFactory().buildColumns(displayList, true)
        val options = TabulatorOptions(
                movableColumns = true,
                height = "calc(100vh - 128px)",
                layout = Layout.FITDATAFILL,
                columns = columns,
                persistenceMode = false//,
                //selectable = true
        )

        val tableTypes = setOf(/*TableType.BORDERED,*/ TableType.STRIPED, TableType.HOVER)

        tabulator(model, options = options, types = tableTypes) {
            setEventListener<Tabulator<Exposer>> {
                tabulatorRowClick = {
                }
            }
        }
    }

}
