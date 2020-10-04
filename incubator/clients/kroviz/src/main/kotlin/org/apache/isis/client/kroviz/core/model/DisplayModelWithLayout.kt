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
package org.apache.isis.client.kroviz.core.model

import org.apache.isis.client.kroviz.layout.Layout
import org.apache.isis.client.kroviz.layout.PropertyLt
import org.apache.isis.client.kroviz.layout.RowLt
import org.apache.isis.client.kroviz.to.Extensions
import org.apache.isis.client.kroviz.to.Property
import org.apache.isis.client.kroviz.to.bs3.Grid

abstract class DisplayModelWithLayout : DisplayModel() {

    var layout: Layout? = null
    var grid: Grid? = null
    var propertyDescriptionList = mutableMapOf<String, String>()
    var propertyList = mutableListOf<Property>()
    var propertyLayoutList = mutableListOf<PropertyLt>()

    override fun canBeDisplayed(): Boolean {
        return when {
            isRendered -> false
            layout == null -> false
            grid == null -> false
            else -> {
                val pls = propertyLayoutList.size
                val pds = propertyDescriptionList.size
                val descriptionsComplete = pds >= pls
                descriptionsComplete
            }
        }
    }

    fun addLayout(layout:Layout) {
        this.layout = layout
        initPropertyLayoutList(layout)
    }

    private fun initPropertyLayoutList(layout:Layout) {
        layout.row.forEach { r ->
            initLayout4Row(r)
        }
    }

    private fun initLayout4Row(r:RowLt) {
        r.cols.forEach { cs ->
            val c = cs.getCol()
            c.fieldSet.forEach { fs ->
                propertyLayoutList.addAll(fs.property)
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

   fun addPropertyDescription(p:Property) {
        val id = p.id
        val e:Extensions = p.extensions!!
        val friendlyName = e.friendlyName
        addPropertyDescription(id, friendlyName)
    }

    fun addPropertyDescription(key: String, value: String) {
        propertyDescriptionList.put(key, value)
    }

    fun addProperty(property:Property) {
        propertyList.add(property)
    }

}
