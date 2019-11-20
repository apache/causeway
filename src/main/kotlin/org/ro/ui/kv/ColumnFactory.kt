package org.ro.ui.kv

import org.ro.core.model.DisplayList
import org.ro.core.model.Exposer
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.events.MouseEventInit
import pl.treksoft.kvision.dropdown.ContextMenu
import pl.treksoft.kvision.html.Button
import pl.treksoft.kvision.html.ButtonStyle
import pl.treksoft.kvision.tabulator.Align
import pl.treksoft.kvision.tabulator.ColumnDefinition
import pl.treksoft.kvision.tabulator.Editor
import pl.treksoft.kvision.tabulator.Formatter
import pl.treksoft.kvision.tabulator.js.Tabulator
import pl.treksoft.kvision.utils.obj

/**
 * Create ColumnDefinitions for Tabulator tables
 */
class ColumnFactory {

    //IMPROVE reduce paddingTop/paddingBottom for tabulator-col-content

    private val checkFormatterParams = obj {
        allowEmpty = true
        allowTruthy = true
        tickElement = "<i class='fa fa-square-o'></i>"
        crossElement = "<i class='fa fa-check-square-o'></i>"
    }

    private val menuFormatterParams = obj {
        allowEmpty = true
        allowTruthy = true
        tickElement = "<i class='fa fa-ellipsis-v'></i>"
        crossElement = "<i class='fa fa-ellipsis-v'></i>"
    }

    fun buildColumns(displayList: DisplayList, withCheckBox: Boolean = false): List<ColumnDefinition<dynamic>> {

        val columns = mutableListOf<ColumnDefinition<dynamic>>()
        if (withCheckBox) {
            val checkBox = buildCheckBox()
            columns.add(checkBox)
        }

        val model = displayList.data as List<dynamic>
        if (model[0].hasOwnProperty("iconName")) {
            val icon = buildLinkIcon()
            columns.add(icon)
        }

        val propertyLabels = displayList.propertyLabels
        for (pl in propertyLabels) {
            val id = pl.key
            val friendlyName = pl.value
            var cd = ColumnDefinition<Exposer>(
                    title = friendlyName,
                    field = id,
                    headerFilter = Editor.INPUT)
            if (id == "object") {
                cd = buildLink()
            }
            columns.add(cd)
        }

        val menu = buildMenu()
        columns.add(menu)

        return columns
    }

    private fun buildLinkIcon(): ColumnDefinition<Exposer> {
        val icon = ColumnDefinition<dynamic>(
                "",
                field = "iconName",
                align = Align.CENTER,
                width = "40",
                formatterComponentFunction = { _, _, data ->
                    Button(text = "", icon = data["iconName"], style = ButtonStyle.LINK).onClick {
                        console.log(data)
                    }
                })
        return icon
    }

    private fun buildLink(): ColumnDefinition<Exposer> {
        return ColumnDefinition<dynamic>(
                title = "ResultListResult",
                field = "result",
                headerFilter = Editor.INPUT,
                formatterComponentFunction = { _, _, data ->
                    Button(text = data["object"].title, icon = "fas fa-star-o", style = ButtonStyle.LINK).onClick {
                        console.log(data)
                    }
                })
    }

    private fun buildCheckBox(): ColumnDefinition<Exposer> {
        return ColumnDefinition<Exposer>(
                title = "",
                field = "selected",
                formatter = Formatter.TICKCROSS,
                formatterParams = checkFormatterParams,
                /*               formatterComponentFunction = { cell, _, _ ->
                                   if (isSelected(cell)) {
                                       obj {"<i class='fa fa-check-square-o'></i>"}
                                   } else {
                                       obj {"<i class='fa fa-square-o'></i>"}
                                   }
                               }, */
                align = Align.CENTER,
                width = "40",
                headerSort = false,
                cellClick = { evt, cell ->
                    evt.stopPropagation()
                    toggleSelection(cell)
                })
    }

    private fun buildMenu(): ColumnDefinition<Exposer> {
        return ColumnDefinition("",
                field = "iconName", // any existing field can be used
                formatter = Formatter.TICKCROSS,
                formatterParams = menuFormatterParams,
                align = Align.CENTER,
                width = "40",
                headerSort = false,
                cellClick = { evt, cell ->
                    evt.stopPropagation()
                    buildContextMenu(evt, cell)
                })
    }

    private fun buildContextMenu(mouseEvent: MouseEvent, cell: Tabulator.CellComponent): ContextMenu {
        val exposer = getData(cell)
        val tObject = exposer.delegate
        val menu = MenuFactory.buildFor(tObject)
        //FIXME x/y coordinates are sometimes off screen
        //val menuWidth =  menu.maxWidth?.first!!.toInt()
/*        val mei = MouseEventInit(
                clientX = (mouseEvent.clientX - 250),
                clientY = (mouseEvent.clientY - 500),
                screenY = (mouseEvent.clientY - 500)) */
        val mei = MouseEventInit(
                clientX = (mouseEvent.clientX - 250),
                clientY = (mouseEvent.clientY - 500),
                screenY = (mouseEvent.clientY - 500))
        val positionedEvent = MouseEvent("", mei)
        positionedEvent.pageX //= 200 as Double
        menu.positionMenu(positionedEvent)
        console.log("[ColFac.showContextMenu]")
        console.log(mouseEvent)
 //       menu.left = 200.px
 //       menu.top = 200.px
        return menu
    }

    private fun getData(cell: Tabulator.CellComponent): Exposer {
        val row = cell.getRow()
        val data = row.getData().asDynamic()
        val exposer = data as Exposer
        return exposer
    }

    private fun toggleSelection(cell: Tabulator.CellComponent) {
        val exposer = getData(cell)
        val oldValue = exposer.selected
        exposer.selected = !oldValue
    }

    private fun isSelected(cell: Tabulator.CellComponent): Boolean {
        return getData(cell).selected
    }

}
