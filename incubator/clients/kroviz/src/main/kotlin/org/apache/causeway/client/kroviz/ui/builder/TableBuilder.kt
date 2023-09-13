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
package org.apache.causeway.client.kroviz.ui.builder

import io.kvision.tabulator.*
import org.apache.causeway.client.kroviz.ui.core.Constants

class TableBuilder {
    fun createTabulator(
        data: MutableList<dynamic>,
        columns: List<ColumnDefinition<dynamic>>
    ): Tabulator<dynamic> {
        val options = createOptions(columns)
        val tabulator = Tabulator(null,
            dataUpdateOnEdit = true,
            options.copy(data = data.toTypedArray()),
            setOf(TableType.STRIPED, TableType.HOVER))
        val className: String? = null
        if (className != null)
            tabulator.addCssClass(className)
        val init: (Tabulator<dynamic>.() -> Unit)? = null
        init?.invoke(tabulator)
        tabulator.addCssClass("horizontal-tb")
        return tabulator
    }

    private fun createOptions(columns: List<ColumnDefinition<dynamic>>): TabulatorOptions<dynamic> {
        return TabulatorOptions(
            movableColumns = true,
            height = Constants.calcHeight,
            layout = Layout.FITCOLUMNS,
            columns = columns,
            persistenceMode = false
        )
    }

}