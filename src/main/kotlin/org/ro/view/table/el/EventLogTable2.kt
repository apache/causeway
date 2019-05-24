package org.ro.view.table.el

import com.github.snabbdom._get
import org.ro.core.event.EventStore
import org.ro.core.event.LogEntry
import org.ro.view.IconManager
import org.ro.view.RoView
import pl.treksoft.kvision.dropdown.ContextMenu
import pl.treksoft.kvision.dropdown.Header.Companion.header
import pl.treksoft.kvision.form.check.RadioGroup
import pl.treksoft.kvision.form.check.RadioGroup.Companion.radioGroup
import pl.treksoft.kvision.form.text.TextInput
import pl.treksoft.kvision.form.text.TextInput.Companion.textInput
import pl.treksoft.kvision.form.text.TextInputType
import pl.treksoft.kvision.html.Link.Companion.link
import pl.treksoft.kvision.i18n.I18n
import pl.treksoft.kvision.panel.FlexAlignItems
import pl.treksoft.kvision.panel.FlexWrap
import pl.treksoft.kvision.panel.HPanel.Companion.hPanel
import pl.treksoft.kvision.panel.VPanel
import pl.treksoft.kvision.tabulator.*
import pl.treksoft.kvision.utils.obj
import pl.treksoft.kvision.utils.px
import pl.treksoft.kvision.tabulator.js.Tabulator as JsTabulator

class EventLogTable2(val model: List<LogEntry>) : VPanel() {
    private lateinit var search: TextInput
    private lateinit var searchTypes: RadioGroup

    private val columns = listOf(
            ColumnDefinition<LogEntry>("", field = "state", width = "80"),
            ColumnDefinition("Title", "title", width = "400"),
            ColumnDefinition("Method", "method", width = "80"),
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
            ColumnDefinition("req.len", field = "requestLength", align = Align.RIGHT),
            ColumnDefinition("offset", field = "offset", align = Align.RIGHT),
            ColumnDefinition("duration", field = "duration", align = Align.RIGHT),
            ColumnDefinition("resp.len", field = "responseLength", align = Align.RIGHT),
            ColumnDefinition("cacheHits", field = "cacheHits", align = Align.RIGHT),
            ColumnDefinition("",
                    field = "iconName",
                    formatter = Formatter.IMAGE,
                    formatterFunction = { cell, _, _ ->
                        cell.getValue()?.let { "<img src='" + cell.getValue() + "'/>" } ?: ""
                    },
                    align = Align.CENTER,
                    width = "40",
                    headerSort = false,
                    cellClick = { evt, cell ->
                        evt.stopPropagation()
                        showDetails(cell)
                    })
    )

    init {
        hPanel(FlexWrap.NOWRAP, alignItems = FlexAlignItems.CENTER, spacing = 20) {
            padding = 10.px
            searchTypes = radioGroup(listOf("all" to "All",
                    "err" to "Errors",
                    "ui" to "UI"), "all", inline = true) {
                marginBottom = 0.px
            }
            search = textInput(TextInputType.SEARCH) {
                placeholder = "Search ..."
            }
        }

        val options = TabulatorOptions(
                height = "calc(100vh - 250px)",
                layout = Layout.FITCOLUMNS,
                columns = columns,
                persistenceMode = false
        )

        val tabulator = Tabulator<LogEntry>(model, options = options)
        marginTop = 0.px
        marginBottom = 0.px
        setEventListener<Tabulator<LogEntry>> {
            tabulatorRowClick = {
            }
        }
        tabulator.setFilter { logEntry ->
            logEntry.match(search.value) && (searchTypes.value == "all" || logEntry.isView())
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
        setContextMenu(buildContextMenu())
    }

    private fun buildContextMenu(): ContextMenu {
        val contextMenu = ContextMenu {
            header(I18n.tr("Actions affecting Tab"))
            val title = "Close"
            val iconName = IconManager.find(title)
            link(I18n.tr(title), icon = iconName) {
                setEventListener {
                    click = { e ->
                        e.stopPropagation()
                        removeTab()
                        this@ContextMenu.hide()
                    }
                }
            }
        }
        return contextMenu;
    }

    private fun removeTab() {
        RoView.remove(this)
    }

    private fun showDetails(cell: pl.treksoft.kvision.tabulator.js.Tabulator.CellComponent) {
        val row = cell.getRow()
        val data = row.getData()
        val url: String = data._get("url")
        val logEntry = EventStore.find(url)!!
        EventLogDetail(logEntry).open()
    }

}