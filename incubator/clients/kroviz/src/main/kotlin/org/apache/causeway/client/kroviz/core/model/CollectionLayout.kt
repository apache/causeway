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

import org.apache.causeway.client.kroviz.core.aggregator.AggregatorWithLayout
import org.apache.causeway.client.kroviz.core.event.ResourceProxy
import org.apache.causeway.client.kroviz.to.ObjectProperty
import org.apache.causeway.client.kroviz.to.PropertyDescription

/**
 * For (parented) collections aggregate information for each column
 * in order to display the table, namely:
 * - columnName :   column header/label
 * - id :           attribute to be used to retrieve the value of each cell (id)
 * - hidden :       will the column be displayed or not
 */
class CollectionLayout : BaseLayout() {
    var numberOfColumns = 0
    private val propertySpecificationMap = mutableMapOf<String, PropertySpecification>()
    private val objectPropertyList = mutableListOf<ObjectProperty>()
    private val propertyDescriptionList = mutableListOf<PropertyDescription>()

    override fun readyToRender(): Boolean {
        console.log("[CL.readyToRender]")
        console.log(this)
        return (numberOfColumns > 0) &&
                (numberOfColumns == propertyDescriptionList.size)
    }

    /**
     * One element is to be added for each column.
     * Duplicates are not allowed.
     */
    override fun addObjectProperty(
        objectProperty: ObjectProperty,
        aggregator: AggregatorWithLayout,
        referrer: String
    ) {
        objectPropertyList.add(objectProperty)
        val l = objectProperty.getDescriptionLink()!!
        // FIXME NPE -> ISIS-2846 ?
        //invoking DN links leads to an error
        val isDn = l.href.contains("datanucleus") // Outdated?
        if (!isDn) {
            ResourceProxy().fetch(l, aggregator, referrer = referrer)
        }
        updatePropertySpecification(objectProperty)
    }

    override fun addPropertyDescription(
        propertyDescription: PropertyDescription,
        aggregator: AggregatorWithLayout,
        referrer: String
    ) {
        console.log("[CL.addPropertyDescription]")
        propertyDescriptionList.add(propertyDescription)
        updatePropertySpecification(propertyDescription)
    }

    private fun updatePropertySpecification(propertyDescription: PropertyDescription) {
        val ps: PropertySpecification = findOrCreateFor(propertyDescription.id)
        ps.addPropertyDescription(propertyDescription)
    }

    private fun updatePropertySpecification(objectProperty: ObjectProperty) {
        val ps: PropertySpecification = findOrCreateFor(objectProperty.id)
        ps.addObjectProperty(objectProperty)
    }

    private fun findOrCreateFor(id: String): PropertySpecification {
        var ps = propertySpecificationMap[id]
        if (ps == null) {
            ps = PropertySpecification(id)
            propertySpecificationMap[id] = ps
        }
        return ps
    }

    fun getColumnDescriptions(): List<PropertySpecification> {
        return propertySpecificationMap.values.toList()
    }

}