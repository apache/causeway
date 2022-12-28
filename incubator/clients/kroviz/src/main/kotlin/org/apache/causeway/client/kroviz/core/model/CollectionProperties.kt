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

import org.apache.causeway.client.kroviz.layout.PropertyLt
import org.apache.causeway.client.kroviz.to.Extensions
import org.apache.causeway.client.kroviz.to.Property
import org.apache.causeway.client.kroviz.to.bs.PropertyBs

class CollectionProperties() {
    val list = mutableListOf<ColumnProperties>()
    var propertyDescriptionList = mutableListOf<Property>()
    var propertyLayoutList = mutableListOf<PropertyLt>()
    var propertyGridList = mutableListOf<PropertyBs>()
    var propertyList = mutableListOf<Property>()
    var descriptionsComplete = false

    fun debug(): String {
        var answer = "descriptionsComplete: $descriptionsComplete"

        answer = answer + "\nlist: ${list.size}"
        console.log("[CP.debug] List of ColumnProperties")
        console.log(list)

        answer = answer + "\npropertyDescriptionList: ${propertyDescriptionList.size}"
        console.log("[CP.debug] List of Property (Description)")
        console.log(propertyDescriptionList)

        answer = answer + "\npropertyLayoutList: ${propertyLayoutList.size}"
        console.log("[CP.debug] List of PropertyLt")
        console.log(propertyLayoutList)

        answer = answer + "\npropertyGridList: ${propertyGridList.size}"
        console.log("[CP.debug] List of PropertyBs")
        console.log(propertyGridList)

        answer = answer + "\npropertyList: ${propertyList.size}"
        console.log("[CP.debug] List of Property")
        console.log(propertyList)

        return answer
    }

    fun readyForDisplay(): Boolean {
        val ps = propertyList.size
        val pls = propertyLayoutList.size
        val pds = propertyDescriptionList.size
        descriptionsComplete = (pds >= pls) && (ps >= pls)
        return descriptionsComplete
    }

    fun addProperty(property: Property) {
        console.log("[CollectionProperties.addProperty]")
        console.log(property)
        propertyList.add(property)
        console.log(propertyList)
        val id = property.id
        val cp = findOrCreate(id)
        cp.property = property
    }

    fun addAllPropertyLayout(layoutList: List<PropertyLt>) {
        propertyLayoutList.addAll(layoutList)
        fun addPropertyLayout(layout: PropertyLt) {
            val id = layout.id!!
            val cp = findOrCreate(id)
            cp.initLayout(layout)
        }
        layoutList.forEach { addPropertyLayout(it) }
    }

    fun addAllPropertyGrid(gridList: List<PropertyBs>) {
//        console.log("[DMWL.addAllPropertyGrid]")
        propertyGridList.addAll(gridList)
        fun addPropertyGrid(grid: PropertyBs) {
//            console.log("[DMWL.addPropertyGrid] -> grid")
//            console.log(grid)
            val id = grid.id
            val cp = findOrCreate(id)
            cp.initGrid(grid)
        }
        gridList.forEach { addPropertyGrid(it) }
    }

    fun addPropertyDescription(description: Property) {
        propertyDescriptionList.add(description)
        val id = description.id
        val cp = findOrCreate(id)
        val e: Extensions = description.extensions!!
        cp.friendlyName = e.getFriendlyName()
    }

    private fun findOrCreate(id: String): ColumnProperties {
        var cp = find(id)
        if (cp == null) {
            cp = ColumnProperties(id)
            list.add(cp)
        }
        return cp
    }

    fun find(id: String): ColumnProperties? {
        return list.find { it.key == id }
    }

}