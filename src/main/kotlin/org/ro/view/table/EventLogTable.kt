package org.ro.view.table

import org.ro.core.event.EventLog
import org.ro.core.event.EventState
import org.ro.core.event.LogEntry
import pl.treksoft.kvision.data.DataContainer
import pl.treksoft.kvision.data.DataContainer.Companion.dataContainer
import pl.treksoft.kvision.data.SorterType
import pl.treksoft.kvision.form.check.RadioGroup
import pl.treksoft.kvision.form.check.RadioGroup.Companion.radioGroup
import pl.treksoft.kvision.form.text.TextInput
import pl.treksoft.kvision.form.text.TextInput.Companion.textInput
import pl.treksoft.kvision.form.text.TextInputType
import pl.treksoft.kvision.html.Icon.Companion.icon
import pl.treksoft.kvision.modal.Alert
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
        for (cd: ColDef in tableSpec) {
            val hc = HeaderCell(cd.name)
            table.addHeaderCell(hc)
        }

        hPanel(FlexWrap.WRAP, alignItems = FlexAlignItems.CENTER, spacing = 20) {
            search = textInput(TextInputType.SEARCH) {
                placeholder = "Search ..."
            }

            types = radioGroup(listOf("all" to "All", "err" to "Errors", "scr" to "Screen"), "all", inline = true) {
                marginBottom = 0.px
            }
            // types can not be removed or created Empty - why?
        }
        val model = EventLog.log
        val factoryBlock = { logEntry: LogEntry, index: Int, _: Any ->
            Row {
                for (cd: ColDef in tableSpec) {
                    val p = cd.property
                    val v = p.get(logEntry)
                    when (v) {
                        is String -> cell(v)
                        is Date -> cell(v.toStringF("HH:mm:ss.SSS"))
                        is EventState -> cell { icon(v.iconName) } 
                        is ActionMenu -> cell {
                            icon(v.iconName) {
                                title = "View Details"
                                setEventListener {
                                    click = { e ->
                                        e.stopPropagation()
                                        Alert.show("Details", logEntry.url + "\n" + logEntry.response) {
                                        }
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
                else -> SorterType.ASC
            }
        }
        dataContainer = dataContainer(
                model, factoryBlock, table, filter = filterBlock, sorter = {
            when (sort) {
                SortBy.F -> it.offset
                else -> it.cacheHits
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
    }

}