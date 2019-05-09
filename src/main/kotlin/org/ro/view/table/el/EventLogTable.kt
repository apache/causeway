package org.ro.view.table.el

import org.ro.core.event.EventState
import org.ro.core.event.EventStore
import org.ro.core.event.LogEntry
import org.ro.view.IconManager
import org.ro.view.RoView
import org.ro.view.table.ActionMenu
import org.ro.view.table.ColDef
import pl.treksoft.kvision.core.CssSize
import pl.treksoft.kvision.core.UNIT
import pl.treksoft.kvision.data.DataContainer
import pl.treksoft.kvision.data.DataContainer.Companion.dataContainer
import pl.treksoft.kvision.data.SorterType
import pl.treksoft.kvision.dropdown.ContextMenu
import pl.treksoft.kvision.dropdown.Header.Companion.header
import pl.treksoft.kvision.form.check.RadioGroup
import pl.treksoft.kvision.form.check.RadioGroup.Companion.radioGroup
import pl.treksoft.kvision.form.text.TextInput
import pl.treksoft.kvision.form.text.TextInput.Companion.textInput
import pl.treksoft.kvision.form.text.TextInputType
import pl.treksoft.kvision.html.Button.Companion.button
import pl.treksoft.kvision.html.ButtonStyle
import pl.treksoft.kvision.html.Icon.Companion.icon
import pl.treksoft.kvision.html.Link.Companion.link
import pl.treksoft.kvision.i18n.I18n
import pl.treksoft.kvision.panel.FlexAlignItems
import pl.treksoft.kvision.panel.FlexWrap
import pl.treksoft.kvision.panel.HPanel.Companion.hPanel
import pl.treksoft.kvision.panel.SimplePanel
import pl.treksoft.kvision.table.Cell.Companion.cell
import pl.treksoft.kvision.table.HeaderCell
import pl.treksoft.kvision.table.Row
import pl.treksoft.kvision.table.Table
import pl.treksoft.kvision.table.TableType
import pl.treksoft.kvision.types.toStringF
import pl.treksoft.kvision.utils.px
import kotlin.js.Date

enum class SortBy {
    F
}

@Deprecated("use tables based on Tabulator")
class EventLogTable(private val tableSpec: List<ColDef>) : SimplePanel() {

    private val dataContainer: DataContainer<LogEntry, Row, Table>
    private lateinit var search: TextInput
    private lateinit var types: RadioGroup
    val table = Table(types = setOf(TableType.STRIPED, TableType.HOVER))
    private var sort = SortBy.F
        set(value) {
            field = value
            dataContainer.update()
        }

    init {
        paddingTop = CssSize(0, UNIT.mm)

        for (cd: ColDef in tableSpec) {
            val hc = HeaderCell(cd.name)
            table.addHeaderCell(hc)
        }

        hPanel(FlexWrap.WRAP, alignItems = FlexAlignItems.CENTER, spacing = 20) {
            //            height = "calc(100vh - 250px)",
            marginTop = 4.px
            marginLeft = 4.px
            // types can not be removed or created Empty - why?
            types = radioGroup(listOf(
                    "all" to "All",
                    "err" to "Errors",
                    "ui" to "UI"),
                    "all",
                    inline = true) {
                marginBottom = 0.px
            }

            search = textInput(TextInputType.SEARCH) {
                placeholder = "Search ..."
            }

        }
        val model = EventStore.log
        val factoryBlock = { logEntry: LogEntry, _: Int, _: Any ->
            Row {
                for (cd: ColDef in tableSpec) {
                    val p = cd.property
                    val v = p.get(logEntry)
                    when (v) {
                        is String -> {
                            title = "A tooltip: $v"
                            enableTooltip()
                        }
                        is Date -> cell(v.toStringF("HH:mm:ss.SSS"))
                        is EventState -> cell {
                            button(I18n.tr(logEntry.title), icon = v.iconName, style = ButtonStyle.LINK)
                        }
                        is ActionMenu -> cell {
                            icon(v.iconName) {
                                title = "View Details"
                                setEventListener {
                                    click = { e ->
                                        e.stopPropagation()
                                        //TODO use RoDialog, cf. ErrorAlert
                                        EventLogDetail(logEntry).open()
                                        //Alert.show("Details", logEntry.url + "\n" + logEntry.response) {     }
                                    }
                                }
                            }
                        }
                        else -> cell(v.toString())
                    }
                }
            }
        }
        val filterBlock = { logEntry: LogEntry ->
            logEntry.match(search.value) && (types.value == "all" || logEntry.isView() ?: false)
        }
        //    val sorterBlock = 
        val sorterTypeBlock = {
            when (sort) {
                SortBy.F -> SorterType.DESC
            }
        }
        dataContainer = dataContainer(
                model, factoryBlock, table, filter = filterBlock, sorter = {
            when (sort) {
                SortBy.F -> it.offset
            }
        }, sorterType = sorterTypeBlock
        )
        search.setEventListener {
            input = {
                dataContainer.update()
            }
        }
        types.setEventListener {
            change = {
                dataContainer.update()
            }
        }

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
        setContextMenu(contextMenu)

    }

    fun removeTab() {
        RoView.remove(this)
    }

}