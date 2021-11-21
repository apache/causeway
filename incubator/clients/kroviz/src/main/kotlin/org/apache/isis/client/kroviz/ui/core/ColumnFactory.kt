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
package org.apache.isis.client.kroviz.ui.core

import io.kvision.tabulator.Align
import io.kvision.tabulator.ColumnDefinition
import io.kvision.tabulator.Editor
import io.kvision.tabulator.Formatter
import io.kvision.tabulator.js.Tabulator
import io.kvision.utils.obj
import org.apache.isis.client.kroviz.core.model.CollectionDM
import org.apache.isis.client.kroviz.core.model.Exposer
import org.apache.isis.client.kroviz.ui.panel.DynamicMenuBuilder

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
        val columns = mutableListOf<ColumnDefinition<Exposer>>()
        addColumnForObjectIcon(displayCollection, columns)
        addColumnsForProperties(displayCollection, columns)
        columns.add(columnForObjectMenu())
        return columns
    }

    private fun columnForObjectMenu(): ColumnDefinition<dynamic> {
        return ColumnDefinition<dynamic>(
                "",
                field = "iconName", // any existing field can be used
                formatter = Formatter.TICKCROSS,
                formatterParams = menuFormatterParams,
                hozAlign = Align.CENTER,
                width = "40",
                headerSort = false,
                clickMenu = { component: dynamic, _: dynamic ->
                    buildObjectMenu(component)
                }
        )
    }

    private fun buildObjectMenu(cell: Tabulator.CellComponent): dynamic {
        val exposer = cell.getData() as Exposer
        val tObject = exposer.delegate
        return DynamicMenuBuilder().buildObjectMenu(tObject)
    }

    private fun addColumnForObjectIcon(displayCollection: CollectionDM, columns: MutableList<ColumnDefinition<Exposer>>) {
        exposeIcons(displayCollection)
        val iconColumn = buildIconColumn()
        columns.add(iconColumn)
    }

    private fun exposeIcons(displayCollection: CollectionDM) {
        val model = displayCollection.data
        val icon = displayCollection.icon
        model.forEach { exposer ->
            if (icon != null) {
                exposer.setIcon(icon)
            }
        }
    }

    private fun buildIconColumn(): ColumnDefinition<Exposer> {
        return ColumnDefinition<dynamic>(
                "",
                field = "icon",
                formatter = Formatter.IMAGE,
                formatterParams = obj { width = "16px"; height = "16px" },
                hozAlign = Align.CENTER,
                width = "40",
                headerSort = false)
    }

    private fun addColumnsForProperties(displayCollection: CollectionDM, columns: MutableList<ColumnDefinition<Exposer>>) {
        val propertyLabels = displayCollection.properties.list
        for (pl in propertyLabels) {
            if (!pl.hidden) {
                val id = pl.key
                val friendlyName = pl.friendlyName
                var cd = ColumnDefinition<dynamic>(
                        title = friendlyName,
                        field = id,
                        headerFilter = Editor.INPUT)
                if (id == "object") {
                    cd = buildLink()
                }
                columns.add(cd)
            }
        }
    }

    private fun buildLink(): ColumnDefinition<Exposer> {
        return ColumnDefinition<dynamic>(
                title = "ResultListResult",
                field = "result",
                headerFilter = Editor.INPUT
        )
    }

}
