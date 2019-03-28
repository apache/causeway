package org.ro.view

import org.ro.core.event.EventLog
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
import pl.treksoft.kvision.modal.Confirm
import pl.treksoft.kvision.panel.FlexAlignItems
import pl.treksoft.kvision.panel.FlexWrap
import pl.treksoft.kvision.panel.HPanel.Companion.hPanel
import pl.treksoft.kvision.panel.SimplePanel
import pl.treksoft.kvision.table.Cell.Companion.cell
import pl.treksoft.kvision.table.HeaderCell
import pl.treksoft.kvision.table.Row
import pl.treksoft.kvision.table.Table
import pl.treksoft.kvision.table.TableType
import pl.treksoft.kvision.utils.px

enum class SortBy {
    FN, LN, E, F
}

class EventLogTable : SimplePanel() {

    private val dataContainer: DataContainer<LogEntry, Row, Table>
    private lateinit var search: TextInput
    private lateinit var types: RadioGroup
    private var sort = SortBy.FN
        set(value) {
            field = value
            dataContainer.update()
        }

    init {
        padding = 10.px

        val table = Table(types = setOf(TableType.STRIPED, TableType.HOVER)) {
            addHeaderCell(HeaderCell("urlTitle") {
                setEventListener {
                    click = {
                        sort = SortBy.FN
                    }
                }
            })
            addHeaderCell(HeaderCell("CreatedAt") {
                setEventListener {
                    click = {
                        sort = SortBy.LN
                    }
                }
            })
            addHeaderCell(HeaderCell("UpdatedAt") {
                setEventListener {
                    click = {
                        sort = SortBy.E
                    }
                }
            })
            addHeaderCell(HeaderCell("") {
                setEventListener {
                    click = {
                        sort = SortBy.F
                    }
                }
            })
            addHeaderCell(HeaderCell(""))
        }

        hPanel(FlexWrap.WRAP, alignItems = FlexAlignItems.CENTER, spacing = 20) {
            search = textInput(TextInputType.SEARCH) {
                placeholder = "Search ..."
            }
            types = radioGroup(listOf("all" to "All", "fav" to "Favourites"), "all", inline = true) {
                marginBottom = 0.px
            }
        }
        val model = EventLog.log
        val factoryBlock = { logEntry: LogEntry, index: Int, _: Any ->
            Row {
                cell(logEntry.url)
                cell(logEntry.createdAt.toDateString())
                cell {
                    icon("fa-times") {
                        title = "Delete"
                        setEventListener {
                            click = { e ->
                                e.stopPropagation()
                                Confirm.show("Are you sure?", "Do you want to delete this address?") {
                                    //                                    EditPanel.delete(index)
                                }
                            }
                        }
                    }
                }
                setEventListener {
                    click = {
                        //                        EditPanel.edit(index)
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
                SortBy.FN -> it.url.toLowerCase()
                SortBy.LN -> it.method?.toLowerCase()
                SortBy.E -> it.cacheHits
                SortBy.F -> it.response
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
