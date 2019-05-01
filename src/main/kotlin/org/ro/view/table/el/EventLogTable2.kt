package org.ro.view.table.el

import kotlinx.serialization.Serializable
import org.ro.core.event.LogEntry
import pl.treksoft.kvision.form.check.RadioGroup
import pl.treksoft.kvision.form.check.RadioGroup.Companion.radioGroup
import pl.treksoft.kvision.form.text.TextInput
import pl.treksoft.kvision.form.text.TextInput.Companion.textInput
import pl.treksoft.kvision.form.text.TextInputType
import pl.treksoft.kvision.panel.FlexAlignItems
import pl.treksoft.kvision.panel.FlexWrap
import pl.treksoft.kvision.panel.HPanel.Companion.hPanel
import pl.treksoft.kvision.panel.VPanel
import pl.treksoft.kvision.tabulator.ColumnDefinition
import pl.treksoft.kvision.tabulator.Layout
import pl.treksoft.kvision.tabulator.Options
import pl.treksoft.kvision.tabulator.Tabulator
import pl.treksoft.kvision.tabulator.Tabulator.Companion.tabulator
import pl.treksoft.kvision.utils.px
import pl.treksoft.kvision.tabulator.js.Tabulator as JsTabulator

@Serializable
data class Row(val c1: String, val c2: String, val c3: String)

class EventLogTable2 : VPanel() {
    private lateinit var search: TextInput
    private lateinit var searchTypes: RadioGroup

    val rows = listOf(
            Row("a", "b", "c"),
            Row("a", "b", "c"),
            Row("a", "b", "c"),
            Row("a", "b", "c"),
            Row("a", "b", "c"),
            Row("a", "b", "c"),
            Row("a", "b", "c"),
            Row("a", "b", "c"),
            Row("a", "b", "c"),
            Row("a", "b", "c"),
            Row("a", "b", "c"),
            Row("a", "b", "c"),
            Row("a", "b", "c"),
            Row("a", "b", "c"),
            Row("a", "b", "c"),
            Row("a", "b", "c"),
            Row("a", "b", "c"),
            Row("a", "b", "c"),
            Row("a", "b", "c"),
            Row("a", "b", "c"),
            Row("a", "b", "c"),
            Row("a", "b", "c"),
            Row("a", "b", "c"),
            Row("a", "b", "c"),
            Row("a", "b", "c"),
            Row("a", "b", "c"),
            Row("a", "b", "c")
    )

    val columnDefinition = listOf(
            ColumnDefinition("Col 1", "c1"),
            ColumnDefinition("Col 2", "c2"),
            ColumnDefinition("Col 3", "c3")
    )

    init {
        hPanel(FlexWrap.NOWRAP, alignItems = FlexAlignItems.CENTER, spacing = 20) {
            padding = 10.px
            search = textInput(TextInputType.SEARCH) {
                placeholder = "Search ..."
            }
            searchTypes = radioGroup(listOf("all" to "All",
                    "err" to "Errors",
                    "ui" to "UI"), "all", inline = true) {
                marginBottom = 0.px
            }
        }
        val tabulator = tabulator(
                rows, Options(
                height = "calc(100vh - 250px)",
                layout = Layout.FITCOLUMNS,
                columns = columnDefinition,
                persistenceMode = false)
        ) {
            marginBottom = 0.px
            setEventListener<Tabulator<LogEntry>> {
                tabulatorRowClick = {
                    //e ->
                    //   EditPanel.edit((e.detail as JsTabulator.RowComponent).getIndex() as Int)
                }
            }
          //  setFilter { logEntry ->
                //logEntry.match(search.value) && (searchTypes.value == "all")
           // }
        }

        search.setEventListener {
            input = {
                tabulator.applyFilter()
            }
        }
        searchTypes.setEventListener {
            change = {
                tabulator.applyFilter()
            }
        }
    }
}