/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.causeway.client.kroviz.ui.core

import io.kvision.core.Component
import io.kvision.panel.VPanel
import io.kvision.tabulator.Align
import io.kvision.tabulator.ColumnDefinition
import io.kvision.tabulator.Editor
import io.kvision.tabulator.Formatter
import io.kvision.tabulator.js.Tabulator
import io.kvision.utils.obj
import org.apache.causeway.client.kroviz.core.model.CollectionDM
import org.apache.causeway.client.kroviz.core.model.PropertyDetails
import org.apache.causeway.client.kroviz.to.TObject
import org.apache.causeway.client.kroviz.to.ValueType
import org.apache.causeway.client.kroviz.to.Vega5
import org.apache.causeway.client.kroviz.ui.menu.DynamicMenuBuilder
import org.apache.causeway.client.kroviz.utils.js.Vega

/**
 * Create ColumnDefinitions for Tabulator tables
 */
class ColumnFactory {

    private val menuFormatterParams = obj {
        allowEmpty = true
        allowTruthy = true
        tickElement = "<i class='fa fa-ellipsis-v'></i>"
        crossElement = "<i class='fa fa-ellipsis-v'></i>"
    }

    fun buildColumns(displayCollection: CollectionDM): List<ColumnDefinition<dynamic>> {
        console.log("[CF_buildColumns]")
        console.log(displayCollection)
        val columns = mutableListOf<ColumnDefinition<dynamic>>()
        columns.add(columnForObjectIcon(displayCollection))
        columns.addAll(columnsForProperties(displayCollection))
        columns.add(columnForObjectMenu())
        return columns
    }

    private fun columnForObjectIcon(displayCollection: CollectionDM): ColumnDefinition<dynamic> {
        exposeIcons(displayCollection)
        return buildIconColumn()
    }

    private fun columnForObjectMenu(): ColumnDefinition<dynamic> {
        return ColumnDefinition(
            "",
            field = "iconName", // any existing field can be used
            formatter = Formatter.TICKCROSS,
            formatterParams = menuFormatterParams,
            hozAlign = Align.CENTER,
            width = "40",
            headerSort = false,
            clickMenu = { _: dynamic, cellComponent: dynamic ->
                buildObjectMenu(cellComponent.unsafeCast<Tabulator.CellComponent>())
            }
        )
    }

    private fun buildObjectMenu(cell: Tabulator.CellComponent): dynamic {
        val row = cell.getRow()
        val dynamic = row.getData().asDynamic()
        val tObject = dynamic.delegate
        return DynamicMenuBuilder().buildObjectMenu(tObject as TObject)
    }

    private fun exposeIcons(displayCollection: CollectionDM) {
        val icon = displayCollection.icon
        displayCollection.data.forEach {
            if (icon != null) {
                it["icon"] = icon
            }
        }
    }

    private fun buildIconColumn(): ColumnDefinition<dynamic> {
        return ColumnDefinition(
            "",
            field = "icon",
            formatter = Formatter.IMAGE,
            formatterParams = obj { width = "16px"; height = "16px" },
            hozAlign = Align.CENTER,
            width = "40",
            headerSort = false
        )
    }

    private fun columnsForProperties(collectionModel: CollectionDM): MutableList<ColumnDefinition<dynamic>> {
        val answer = mutableListOf<ColumnDefinition<dynamic>>()
        val clo = collectionModel.collectionLayout
        val propSpecList = clo.propertyDetailsList
        if (propSpecList.size == 0) {
            // without this, propSpecList is empty? problem with mutable list?
            throw IllegalStateException()
        }
        propSpecList.forEach {
            if (!it.hidden) {
                val colDef = buildColumnDefinition(it)
                answer.add(colDef)
            }
        }
        return answer
    }

    private fun buildColumnDefinition(pd: PropertyDetails): ColumnDefinition<dynamic> {
        return when {
            pd.id == "object" -> buildLink()
            pd.type == ValueType.CANVAS.type -> buildVega(pd)
            else -> buildDefault(pd)
        }
    }

    private fun buildDefault(it: PropertyDetails): ColumnDefinition<dynamic> {
        return ColumnDefinition(
            title = it.name,
            field = it.id,
            width = (it.typicalLength * 8).toString(),
            headerFilter = Editor.INPUT
        )
    }

    private fun buildVega(it: PropertyDetails): ColumnDefinition<dynamic> {
        return ColumnDefinition(
            title = it.name,
            field = it.id,
            width = (it.typicalLength * 8).toString(),
            headerFilter = Editor.INPUT,
            formatterComponentFunction = { cellComponent: dynamic, _, _: dynamic ->
                console.log("[CF_buildVega]")
                console.log(cellComponent)
                this.buildDiagramPanel(cellComponent.unsafeCast<Tabulator.CellComponent>())
            })
    }

    private fun buildDiagramPanel(cellComponent: Tabulator.CellComponent): Component {
        console.log("[CF_buildDiagramPanel]")
        console.log(cellComponent)
        val panel = VPanel()
        panel.addAfterInsertHook {
            val row = cellComponent.getRow()
            val dynamic = row.getData().asDynamic()
            val json = dynamic.get("readOnlyProperty") as String
            val spec = JSON.parse<Vega5>(json)
            val view = Vega.View(Vega.parse(spec), obj {
                this.renderer = "canvas"
                this.container = getElement()
                this.hover = true
            })
            view.runAsync()
        }
        return panel
    }

    private fun buildLink(): ColumnDefinition<dynamic> {
        return ColumnDefinition(
            title = "ResultListResult",
            field = "result",
            headerFilter = Editor.INPUT
        )
    }

}
