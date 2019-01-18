package com.example

import pl.treksoft.kvision.core.FontStyle
import pl.treksoft.kvision.data.DataContainer
import pl.treksoft.kvision.data.DataContainer.Companion.dataContainer
import pl.treksoft.kvision.form.check.RadioGroup
import pl.treksoft.kvision.form.check.RadioGroup.Companion.radioGroup
import pl.treksoft.kvision.form.text.TextInput
import pl.treksoft.kvision.form.text.TextInput.Companion.textInput
import pl.treksoft.kvision.form.text.TextInputType
import pl.treksoft.kvision.html.Icon.Companion.icon
import pl.treksoft.kvision.html.Link.Companion.link
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

enum class Sort {
    FN, LN, E, F
}

object ListPanel : SimplePanel() {

    private val container: DataContainer<Address, Row>
    private lateinit var search: TextInput
    private lateinit var types: RadioGroup
    private var sort = Sort.FN
        set(value) {
            field = value
            container.update()
        }

    init {
        padding = 10.px

        val table = Table(types = setOf(TableType.STRIPED, TableType.HOVER)) {
            addHeaderCell(HeaderCell("First name") {
                setEventListener {
                    click = {
                        sort = Sort.FN
                    }
                }
            })
            addHeaderCell(HeaderCell("Last name") {
                setEventListener {
                    click = {
                        sort = Sort.LN
                    }
                }
            })
            addHeaderCell(HeaderCell("E-mail") {
                setEventListener {
                    click = {
                        sort = Sort.E
                    }
                }
            })
            addHeaderCell(HeaderCell("") {
                setEventListener {
                    click = {
                        sort = Sort.F
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

        container = dataContainer(
                Model.addresses, { index, address ->
                Row {
                    cell(address.firstName)
                    cell(address.lastName)
                    cell {
                        address.email?.let {
                            link(it, "mailto:$it") {
                                fontStyle = FontStyle.ITALIC
                            }
                        }
                    }
                    cell {
                        address.favourite?.let {
                            if (it) icon("fa-heart-o") {
                                title = "Favourite"
                            }
                        }
                    }
                    cell {
                        icon("fa-times") {
                            title = "Delete"
                            setEventListener {
                                click = { e ->
                                    e.stopPropagation()
                                    Confirm.show("Are you sure?", "Do you want to delete this address?") {
                                        EditPanel.delete(index)
                                    }
                                }
                            }
                        }
                    }
                    setEventListener {
                        click = {
                            EditPanel.edit(index)
                        }
                    }
                }
            }, { _, address ->
                address.match(search.value) && (types.value == "all" || address.favourite ?: false)
            }, {
                when (sort) {
                    Sort.FN -> it.sortedBy { it.second.firstName?.toLowerCase() }
                    Sort.LN -> it.sortedBy { it.second.lastName?.toLowerCase() }
                    Sort.E -> it.sortedBy { it.second.email?.toLowerCase() }
                    Sort.F -> it.sortedBy { it.second.favourite }
                }
            }, container = table
        )
        search.setEventListener {
            input = {
                container.update()
            }
        }
        types.setEventListener {
            change = {
                container.update()
            }
        }
    }
}
