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
package org.apache.causeway.client.kroviz.core.model

import org.apache.causeway.client.kroviz.layout.Layout
import org.apache.causeway.client.kroviz.layout.RowLt
import org.apache.causeway.client.kroviz.to.Icon
import org.apache.causeway.client.kroviz.to.Property
import org.apache.causeway.client.kroviz.to.TransferObject
import org.apache.causeway.client.kroviz.to.bs3.Grid
import org.apache.causeway.client.kroviz.to.bs3.Row

abstract class DisplayModelWithLayout : BaseDisplayModel() {

    var layout: Layout? = null
    var grid: Grid? = null
    val properties = CollectionProperties()
    var icon: Icon? = null

    override fun canBeDisplayed(): Boolean {
//        console.log("[DMWL.canBeDisplayed]")
//        console.log(this)
        return when {
            isRendered -> false
            layout != null -> true
            grid != null -> true
            else -> properties.readyForDisplay()
        }
    }

    fun addLayout(layout: Layout) {
        this.layout = layout
        initPropertyLayoutList(layout)
    }

    fun addGrid(grid: Grid) {
        console.log("[DMWL.initGrid]")
        console.log(grid)
        this.grid = grid
        initPropertyGridList(grid)
    }

    fun addIcon(obj: TransferObject?) {
        icon = obj as Icon
    }

    private fun initPropertyLayoutList(layout: Layout) {
        layout.row.forEach { r ->
            initLayout4Row(r)
        }
    }

    private fun initPropertyGridList(grid: Grid) {
        grid.rows.forEach { r ->
            initGrid4Row(r)
        }
    }

    private fun initLayout4Row(r: RowLt) {
        r.cols.forEach { cs ->
            val c = cs.getCol()
            c.fieldSet.forEach { fs ->
                properties.addAllPropertyLayout(fs.property)
            }
            c.tabGroup.forEach { tg ->
                tg.tab.forEach { t ->
                    t.row.forEach { r2 ->
                        initLayout4Row(r2)
                    }
                }
            }
        }
    }

    private fun initGrid4Row(r: Row) {
        r.colList.forEach { c ->
            c.fieldSetList.forEach { fs ->
                //FIXME         properties.addAllPropertiesFromGrid(fs.propertyList)
            }
            c.tabGroupList.forEach { tg ->
                tg.tabList.forEach { t ->
                    t.rowList.forEach { r2 ->
                        initGrid4Row(r2)
                    }
                }
            }
        }
    }

    fun addPropertyDescription(p: Property) {
        properties.addPropertyDescription(p)
    }

    fun addProperty(property: Property) {
        properties.addProperty(property)
    }

}
