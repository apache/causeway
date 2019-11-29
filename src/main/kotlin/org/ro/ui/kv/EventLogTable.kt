package org.ro.ui.kv

import org.ro.core.event.EventStore
import org.ro.core.event.LogEntry
import org.ro.ui.table.el.EventLogDetail
import pl.treksoft.kvision.html.Button
import pl.treksoft.kvision.html.ButtonStyle
import pl.treksoft.kvision.panel.FlexAlignItems
import pl.treksoft.kvision.panel.FlexWrap
import pl.treksoft.kvision.panel.VPanel
import pl.treksoft.kvision.panel.hPanel
import pl.treksoft.kvision.tabulator.*
import pl.treksoft.kvision.utils.obj
import pl.treksoft.kvision.utils.px

class EventLogTable(val model: List<LogEntry>) : VPanel() {

    private val faFormatterParams = obj {
        allowEmpty = true
        allowTruthy = true
        tickElement = "<i class='fa fa-ellipsis-v'></i>"
        crossElement = "<i class='fa fa-ellipsis-v'></i>"
    }

    private val columns = listOf(
            ColumnDefinition<LogEntry>("Title", "title",
                    headerFilter = Editor.INPUT,
                    formatterComponentFunction = { _, _, data ->
                        Button(data.title, icon = data.state.iconName, style = ButtonStyle.LINK).onClick {
                            console.log(data)
                        }
                    }),
            ColumnDefinition("State", "state", width = "100", headerFilter = Editor.INPUT),
            ColumnDefinition("Method", "method", width = "100"),
            ColumnDefinition("req.len", field = "requestLength", width = "100", align = Align.RIGHT),
            ColumnDefinition("resp.len", field = "responseLength", width = "100", align = Align.RIGHT),
            ColumnDefinition(
                    title = "Created",
                    field = "createdAt",
                    sorter = Sorter.DATETIME,
                    formatter = Formatter.DATETIME,
                    formatterParams = obj { outputFormat = "HH:mm:ss.SSS" },
                    width = "100"),
            ColumnDefinition(
                    title = "Updated",
                    field = "updatedAt",
                    sorter = Sorter.DATETIME,
                    formatter = Formatter.DATETIME,
                    formatterParams = obj { outputFormat = "HH:mm:ss.SSS" },
                    width = "100"),
            ColumnDefinition("duration", field = "duration", width = "100", align = Align.RIGHT),
            ColumnDefinition("cacheHits", field = "cacheHits", width = "100", align = Align.RIGHT),
            ColumnDefinition("Details",
                    field = "title", // any existing field can be used
                    formatter = Formatter.TICKCROSS,
                    formatterParams = faFormatterParams,
                    align = Align.CENTER,
                    width = "100",
                    headerSort = false,
                    cellClick = { evt, cell ->
                        evt.stopPropagation()
                        showDetails(cell)
                    })
    )

    init {
        hPanel(FlexWrap.NOWRAP,
                alignItems = FlexAlignItems.CENTER,
                spacing = 20) {
            padding = 10.px
            paddingTop = 0.px
        }

        val options = TabulatorOptions(
                height = "calc(100vh - 128px)",
                layout = Layout.FITCOLUMNS,
                columns = columns,
                persistenceMode = false
        )

        tabulator(model, options = options) {
            marginTop = 0.px
            marginBottom = 0.px
            setEventListener<Tabulator<LogEntry>> {
                tabulatorRowClick = {
                }
            }
        }
    }

    private fun showDetails(cell: pl.treksoft.kvision.tabulator.js.Tabulator.CellComponent) {
        val row = cell.getRow()
        val data = row.getData() as Map<String, String>
        val url: String = data.get("url")!!
        val logEntry = EventStore.find(url)!!
        EventLogDetail(logEntry).open()
    }

}
