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

@Deprecated("Rework/Rename")
class ObjectSpecificationHolder {
    val list = mutableListOf<ColumnProperties>()
    private var propertyDescriptionList = mutableListOf<Property>()
    private var propertyLayoutList = mutableListOf<PropertyLt>()
    var propertyList = mutableListOf<Property>()
    private var descriptionsComplete = false

    fun readyToRender(): Boolean {
        val ps = propertyList.size
        val pls = propertyLayoutList.size
        val pds = propertyDescriptionList.size
        descriptionsComplete = (pds >= pls) && (ps >= pls)
        return descriptionsComplete
    }

    fun addProperty(property: Property) {
        propertyList.add(property)
        val id = property.id
        val cp = findOrCreate(id)
        cp.property = property
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