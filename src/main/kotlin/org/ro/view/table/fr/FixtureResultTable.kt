package org.ro.view.table.fr

import com.github.snabbdom._get
import org.ro.core.event.LogEntry
import org.ro.core.model.ObjectAdapter
import org.ro.to.TObject
import org.ro.view.IconManager
import org.ro.view.RoView
import pl.treksoft.kvision.dropdown.ContextMenu
import pl.treksoft.kvision.dropdown.Header.Companion.header
import pl.treksoft.kvision.form.check.RadioGroup
import pl.treksoft.kvision.form.text.TextInput
import pl.treksoft.kvision.form.text.TextInput.Companion.textInput
import pl.treksoft.kvision.form.text.TextInputType
import pl.treksoft.kvision.html.Link.Companion.link
import pl.treksoft.kvision.i18n.I18n
import pl.treksoft.kvision.panel.FlexAlignItems
import pl.treksoft.kvision.panel.FlexWrap
import pl.treksoft.kvision.panel.HPanel.Companion.hPanel
import pl.treksoft.kvision.panel.VPanel
import pl.treksoft.kvision.tabulator.ColumnDefinition
import pl.treksoft.kvision.tabulator.Layout
import pl.treksoft.kvision.tabulator.Options
import pl.treksoft.kvision.tabulator.Tabulator
import pl.treksoft.kvision.tabulator.Tabulator.Companion.tabulator
import pl.treksoft.kvision.utils.px

fun ObjectAdapter.match(search: String?): Boolean {
    return search?.let {
        resultClass.contains(it, true) ?: false 
    } ?: true      
}

class FixtureResultTable (val model: List<ObjectAdapter>) : VPanel() {
    private lateinit var search: TextInput
    private lateinit var searchTypes: RadioGroup

    private val columns = listOf(
            ColumnDefinition("", field = "icon", width = "40"),
            ColumnDefinition("Result Class", "resultClass"),
            ColumnDefinition("Fixture Script", "fixtureScript"),
            ColumnDefinition("Result Key", field = "resultKey"),
            ColumnDefinition("Result", field = "result")
    )

    init {
        hPanel(FlexWrap.NOWRAP, alignItems = FlexAlignItems.CENTER, spacing = 20) {
            padding = 10.px
            search = textInput(TextInputType.SEARCH) {
                placeholder = "Search ..."
            }
        }

        val options = Options(
                height = "calc(100vh - 250px)",
                layout = Layout.FITCOLUMNS,
                columns = columns,
                persistenceMode = false
        )

        val tabulator = tabulator(model, options) {
            marginTop = 0.px
            marginBottom = 0.px
            setEventListener<Tabulator<LogEntry>> {
                tabulatorRowClick = {
                }
            }
            setFilter { result ->
                result.match(search.value)
            }
        }

        search.setEventListener {
            input = {
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
//        val row = cell.getRow()
//        val data = row.getData()
//        val url: String = data._get("url")
//        val logEntry = EventStore.find(url)!!
//        EventLogDetail(logEntry).open()
    }

}