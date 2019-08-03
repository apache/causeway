package org.ro.view.table.fr

import org.ro.core.model.ObjectAdapter
import pl.treksoft.kvision.html.Button
import pl.treksoft.kvision.panel.FlexAlignItems
import pl.treksoft.kvision.panel.FlexWrap
import pl.treksoft.kvision.panel.HPanel
import pl.treksoft.kvision.panel.VPanel
import pl.treksoft.kvision.tabulator.ColumnDefinition
import pl.treksoft.kvision.tabulator.Layout
import pl.treksoft.kvision.tabulator.Tabulator
import pl.treksoft.kvision.tabulator.Tabulator.Companion.tabulator
import pl.treksoft.kvision.tabulator.TabulatorOptions
import pl.treksoft.kvision.utils.px

class FixtureResultTable(val model: List<ObjectAdapter>) : VPanel() {

    private val columns = listOf(
            ColumnDefinition<ObjectAdapter>("", field = "iconName", width = "40",
                    formatterComponentFunction = { _, _, data ->
                        Button(text = "", icon = data.iconName).onClick {
                            console.log(data.result)
                        }
                    }),
            ColumnDefinition("Result Class", "resultClass"),
            ColumnDefinition("Fixture Script", "fixtureScript"),
            ColumnDefinition("Result Key", field = "resultKey"),
            ColumnDefinition("Result", field = "result")
    )

    init {
        HPanel(
                FlexWrap.NOWRAP,
                alignItems = FlexAlignItems.CENTER,
                spacing = 20) {
            padding = 10.px
        }

        val options = TabulatorOptions(
                height = "calc(100vh - 250px)",
                layout = Layout.FITCOLUMNS,
                columns = columns,
                persistenceMode = false
        )

        tabulator(
                model, options = options) {
            marginTop = 0.px
            marginBottom = 0.px
            setEventListener<Tabulator<ObjectAdapter>> {
                tabulatorRowClick = {
                }
            }
        }
    }

}
