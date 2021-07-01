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

import org.apache.isis.client.kroviz.layout.PropertyLt
import org.apache.isis.client.kroviz.to.Extensions
import org.apache.isis.client.kroviz.to.Property

class Properties() {
    val list = mutableListOf<PropertyFacade>()

    fun addProperty(property: Property) {
        val id = property.id
        val pf = findOrCreate(id)
        pf.property = property
    }

    fun addAllPropertyLayout(layoutList: List<PropertyLt>) {
        fun addPropertyLayout(layout: PropertyLt) {
            val id = layout.id!!
            val pf = findOrCreate(id)
            pf.layout = layout
        }
        layoutList.forEach { addPropertyLayout(it) }
    }

    fun addPropertyDescription(description: Property) {
        val id = description.id
        val pf = findOrCreate(id)
        val e: Extensions = description.extensions!!
        pf.friendlyName = e.friendlyName
    }

    private fun findOrCreate(id: String): PropertyFacade {
        var pf = list.find { it.key == id }
        if (pf == null) {
            pf = PropertyFacade(id)
            list.add(pf)
        }
        return pf
    }

}

/**
 * Properties have three aspects:
 *
 * - Member of a DomainObject
 * - Description (labels, friendlyName)
 * - Layout
 *
 * All three are required in order to display correctly in a table.
 */
class PropertyFacade(val key: String) {
    var property: Property? = null
    var friendlyName: String = ""
    var layout: PropertyLt? = null

    fun hidden(): Boolean {
        if (layout != null) {
            return (layout!!.hidden != null)
        }
        return false
    }

}
