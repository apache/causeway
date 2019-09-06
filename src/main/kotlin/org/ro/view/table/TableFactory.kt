package org.ro.view.table

import org.ro.core.model.Exposer
import pl.treksoft.kvision.html.Button
import pl.treksoft.kvision.html.ButtonStyle
import pl.treksoft.kvision.tabulator.Align
import pl.treksoft.kvision.tabulator.ColumnDefinition
import pl.treksoft.kvision.tabulator.Formatter
import pl.treksoft.kvision.utils.obj

/**
 * Create ColumnDefinitions for Tabulator tables based on result of REST service invocations
 */
class TableFactory {

    private val faFormatterParams = obj {
        allowEmpty = true;
        allowTruthy = true;
        tickElement = "<i class='fa fa-square-o'></i>";
        crossElement = "<i class='fa fa-check'></i>"
    }

    fun buildColumns(members: Map<String, String>, withCheckBox: Boolean = false): List<ColumnDefinition<Exposer>> {
        val columns = mutableListOf<ColumnDefinition<Exposer>>()
        if (withCheckBox) {
            val checkBox = ColumnDefinition<Exposer>(
                    title = "selected", //TODO add 'selected' attribute dynamically???
                    field = "key",
                    formatter = Formatter.TICKCROSS,
                    formatterParams = faFormatterParams,
                    align = Align.CENTER,
                    width = "100",
                    headerSort = false,
                    cellClick = { evt, cell ->
                        evt.stopPropagation()
                        showDetails(cell)
                    })
            columns.add(checkBox)
        }
        for (m in members) {
            val columnDefinition = when (m.value) {
                "iconName" -> ColumnDefinition<Exposer>(
                        "",
                        field = "iconName",
                        width = "40",
                        formatterComponentFunction = { _, _, data ->
                            Button(text = "", icon = data.iconName, style = ButtonStyle.LINK).onClick {
                                console.log(data)
                            }
                        })
                else -> ColumnDefinition<Exposer>(
                        title = m.key,
                        field = m.value
                )
            }
            columns.add(columnDefinition)
        }
        return columns
    }

    private fun showDetails(cell: pl.treksoft.kvision.tabulator.js.Tabulator.CellComponent) {
        val row = cell.getRow()
        val data = row.getData()
        console.log("[FT.showDetails] $data")
    }

}
