package org.apache.isis.client.kroviz.ui.kv

import org.apache.isis.client.kroviz.core.model.Exposer
import org.apache.isis.client.kroviz.core.model.ListDM
import pl.treksoft.kvision.core.CssSize
import pl.treksoft.kvision.core.UNIT
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
class RoTable(displayList: ListDM) : SimplePanel() {

    private val calcHeight = "calc(100vh - 128px)"

    init {
        title = displayList.extractTitle()
        width = CssSize(100, UNIT.perc)
        val model = displayList.data
        val columns = ColumnFactory().buildColumns(
                displayList,
                true)
        val options = TabulatorOptions(
                movableColumns = true,
                height = calcHeight,
                layout = Layout.FITCOLUMNS,
                columns = columns,
                persistenceMode = false//,
                //selectable = true
        )

        val tableTypes = setOf(TableType.STRIPED, TableType.HOVER)

        tabulator(model, options = options, types = tableTypes) {
            setEventListener<Tabulator<Exposer>> {
                tabulatorRowClick = {
                }
            }
        }
    }

}
