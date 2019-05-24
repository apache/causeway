package org.ro.view.table.fr

import org.ro.core.event.LogEntry
import org.ro.core.model.ObjectAdapter
import pl.treksoft.kvision.form.text.TextInput
import pl.treksoft.kvision.form.text.TextInput.Companion.textInput
import pl.treksoft.kvision.form.text.TextInputType
import pl.treksoft.kvision.panel.FlexAlignItems
import pl.treksoft.kvision.panel.FlexWrap
import pl.treksoft.kvision.panel.HPanel
import pl.treksoft.kvision.panel.VPanel
import pl.treksoft.kvision.tabulator.ColumnDefinition
import pl.treksoft.kvision.tabulator.Layout
import pl.treksoft.kvision.tabulator.Tabulator
import pl.treksoft.kvision.tabulator.TabulatorOptions
import pl.treksoft.kvision.utils.px

class FixtureResultTable(val model: List<ObjectAdapter>) : VPanel() {
    private var search: TextInput

    private val columns = listOf(
            ColumnDefinition<ObjectAdapter>("", field = "icon", width = "40"),
            ColumnDefinition("Result Class", "resultClass"),
            ColumnDefinition("Fixture Script", "fixtureScript"),
            ColumnDefinition("Result Key", field = "resultKey"),
            ColumnDefinition("Result", field = "result")
    )

    private val options = TabulatorOptions(
            height = "calc(100vh - 250px)",
            layout = Layout.FITCOLUMNS,
            columns = columns,
            persistenceMode = false
    )

    val tabulator = Tabulator(model, options = options)

    val hPanel = HPanel(FlexWrap.NOWRAP, alignItems = FlexAlignItems.CENTER, spacing = 20)

    init {
        hPanel.padding = 10.px
        search = textInput(TextInputType.SEARCH) {
            placeholder = "Search ..."
        }

        tabulator.marginTop = 0.px
        tabulator.marginBottom = 0.px
        setEventListener<Tabulator<LogEntry>> {
            tabulatorRowClick = {
            }
        }

        tabulator.setFilter { result ->
            result.match(search.value)
        }

        search.setEventListener {
            input = {
                tabulator.applyFilter()
            }
        }
    }

}