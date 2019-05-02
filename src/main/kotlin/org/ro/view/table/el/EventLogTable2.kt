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
import pl.treksoft.kvision.tabulator.Tabulator.Companion.tabulator
import pl.treksoft.kvision.utils.px
import pl.treksoft.kvision.tabulator.js.Tabulator as JsTabulator

class EventLogTable2 : VPanel() {
    private lateinit var search: TextInput
    private lateinit var searchTypes: RadioGroup

    val columns = listOf(
            ColumnDefinition("", field = "state", width = "40"),
            ColumnDefinition("Title", "title"),
            ColumnDefinition("Method", "method", width = "80"),
            ColumnDefinition("Created", "createdAt", formatter = Formatter.DATETIME, sorter = Sorter.DATETIME),
            ColumnDefinition("Updated", "updatedAt", formatter = Formatter.DATETIME, sorter = Sorter.DATETIME),
            ColumnDefinition("req.len", field = "requestLength", align = Align.RIGHT, width = "60"),
            ColumnDefinition("offset", field = "offset", align = Align.RIGHT, width = "40"),
            ColumnDefinition("duration", field = "duration", align = Align.RIGHT, width = "60"),
            ColumnDefinition("resp.len", field = "responseLength", align = Align.RIGHT, width = "70"),
            ColumnDefinition("cacheHits", field = "cacheHits", align = Align.RIGHT, width = "40"),
            ColumnDefinition("",
                    formatter = Formatter.BUTTONTICK,
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
            search = textInput(TextInputType.SEARCH) {
                placeholder = "Search ..."
            }
            searchTypes = radioGroup(listOf("all" to "All",
                    "err" to "Errors",
                    "ui" to "UI"), "all", inline = true) {
                marginBottom = 0.px
            }
        }

        val model = EventStore.log
        val options = Options(
                height = "calc(100vh - 250px)",
                layout = Layout.FITCOLUMNS,
                columns = columns,
                persistenceMode = false
        )

        val tabulator = tabulator(model, options) {
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