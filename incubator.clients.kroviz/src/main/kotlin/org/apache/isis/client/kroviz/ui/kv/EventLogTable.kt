package org.apache.isis.client.kroviz.ui.kv

import org.apache.isis.client.kroviz.core.event.LogEntry
import pl.treksoft.kvision.core.CssSize
import pl.treksoft.kvision.core.UNIT
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

    private val columns = listOf(
            ColumnDefinition(title = "",
                    field = "state",
                    width = "50",
                    align = Align.CENTER,
                    formatterComponentFunction = { _, _, data ->
                        Button("", icon = "fa fa-ellipsis-v", style = data.state.style).onClick {
                            org.apache.isis.client.kroviz.ui.EventLogDetail(data).open()
                        }.apply { margin = CssSize(-10, UNIT.px) }
                    }),
            ColumnDefinition<LogEntry>("Title", "title",
                    headerFilter = Editor.INPUT,
                    width = "450",
                    formatterComponentFunction = { _, _, data ->
                        Button(data.title, icon = data.state.iconName, style = ButtonStyle.LINK).onClick {
                            console.log(data)
                        }
                    }),
            ColumnDefinition("State", "state", width = "100", headerFilter = Editor.INPUT),
            ColumnDefinition("Method", "method", width = "100", headerFilter = Editor.INPUT),
            ColumnDefinition("req.len", field = "requestLength", width = "100", align = Align.RIGHT),
            ColumnDefinition("resp.len", field = "responseLength", width = "100", align = Align.RIGHT),
            ColumnDefinition("cacheHits", field = "cacheHits", width = "100", align = Align.RIGHT),
            ColumnDefinition("duration", field = "duration", width = "100", align = Align.RIGHT),
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
                    width = "100")
    )

    init {
        hPanel(FlexWrap.NOWRAP,
                alignItems = FlexAlignItems.CENTER,
                spacing = 20) {
            padding = 10.px
            paddingTop = 0.px
        }

        val options = TabulatorOptions(
                movableColumns = true,
                height = "calc(100vh - 128px)",
                layout = Layout.FITDATAFILL,
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

}
