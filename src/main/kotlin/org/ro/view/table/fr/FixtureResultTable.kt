package org.ro.view.table.fr

import org.ro.core.model.ResultExposer
import pl.treksoft.kvision.html.Button
import pl.treksoft.kvision.html.ButtonStyle
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


class FixtureResultTable(
        val model: List<ResultExposer>) : VPanel() {

    private val columns = listOf(
            ColumnDefinition<ResultExposer>("", field = "iconName", width = "40",
                    formatterComponentFunction = { _, _, data ->
                        Button(text = "", icon = data.iconName, style = ButtonStyle.LINK).onClick {
                            console.log(data.result)
                        }
                    }),
            ColumnDefinition("Result Class", "resultClass"),
            ColumnDefinition("Fixture Script", "fixtureScript"),
            ColumnDefinition("Result Key", field = "resultKey"),
            ColumnDefinition("Result", "result",
                    formatterComponentFunction = { _, _, data ->
                        Button(data.result, icon = "fa-star", style = ButtonStyle.LINK).onClick {
                            console.log(data.result)
                        }
                    })
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
            setEventListener<Tabulator<ResultExposer>> {
                tabulatorRowClick = {
                }
            }
        }
    }

}
