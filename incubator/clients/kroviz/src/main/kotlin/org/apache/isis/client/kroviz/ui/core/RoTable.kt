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

import org.apache.isis.client.kroviz.core.model.Exposer
import org.apache.isis.client.kroviz.core.model.CollectionDM
import org.apache.isis.client.kroviz.utils.Utils
import io.kvision.core.Container
import io.kvision.core.CssSize
import io.kvision.core.UNIT
import io.kvision.panel.SimplePanel
import io.kvision.table.TableType
import io.kvision.tabulator.Layout
import io.kvision.tabulator.Tabulator
import io.kvision.tabulator.TabulatorOptions
import io.kvision.utils.set

/**
 * access attributes from dynamic (JS) objects with varying
 * - numbers of attributes
 * - attribute types (can only be determined at runtime) and
 * - accessor names
 */
class RoTable(displayCollection: CollectionDM) : SimplePanel() {

    init {
        title = Utils.extractTitle(displayCollection.title)
        width = CssSize(100, UNIT.perc)
        val model = displayCollection.data
//        val model = buildModel(displayCollection)
        val columns = ColumnFactory().buildColumns(
                displayCollection,
                true)
        val options = TabulatorOptions(
                movableColumns = true,
                height = Constants.calcHeight,
                layout = Layout.FITCOLUMNS,
                columns = columns,
                persistenceMode = false//,
                //selectable = true
        )

        val tableTypes = setOf(TableType.STRIPED, TableType.HOVER)

        tabulator(model, options = options, types = tableTypes) {
            setEventListener<Tabulator<Exposer>> {
                tabulatorRowClick = {
                }
            }
        }
    }

    private fun buildModel(displayCollection: CollectionDM) : List<dynamic> {
        console.log("[RT.buildModel]")
        val model = mutableListOf<Exposer>()
        displayCollection.data.forEach {
            console.log(it)
            val record = it.asDynamic()
            console.log(record["readOnlyProperty"])
            model.add(record)
            console.log(record)
        }
        return model
    }

    fun <T : Any> Container.tabulator(
            data: List<T>? = null,
            dataUpdateOnEdit: Boolean = true,
            options: TabulatorOptions<T> = TabulatorOptions(),
            types: Set<TableType> = setOf(),
            classes: Set<String>? = null,
            className: String? = null,
            init: (Tabulator<T>.() -> Unit)? = null
    ): Tabulator<T> {
        val tabulator = create(data, dataUpdateOnEdit, options, types, classes ?: className.set)
        init?.invoke(tabulator)
        this.add(tabulator)
        return tabulator
    }

    fun <T : Any> create(
            data: List<T>? = null,
            dataUpdateOnEdit: Boolean = true,
            options: TabulatorOptions<T> = TabulatorOptions(),
            types: Set<TableType> = setOf(),
            classes: Set<String> = setOf(),
            init: (Tabulator<T>.() -> Unit)? = null
    ): Tabulator<T> {
        val tabulator = Tabulator(data, dataUpdateOnEdit, options, types, classes)
        init?.invoke(tabulator)
        return tabulator
    }

}
