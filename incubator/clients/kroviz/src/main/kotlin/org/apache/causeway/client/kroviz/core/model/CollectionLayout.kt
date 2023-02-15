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

import org.apache.causeway.client.kroviz.core.aggregator.CollectionAggregator
import org.apache.causeway.client.kroviz.core.event.ResourceProxy
import org.apache.causeway.client.kroviz.to.TObject
import org.apache.causeway.client.kroviz.to.bs.PropertyBs

/**
 * For collections aggregate information for each column
 * in order to display the table, namely:
 * - columnName :   column header/label
 * - id :           attribute to be used to retrieve the value of each cell (id)
 * - hidden :       will the column be displayed or not
 */
class CollectionLayout : BaseLayout() {
    var id = ""
    var propertySpecificationList = mutableListOf<PropertySpecification>()

    override fun readyToRender(): Boolean {
        return isInitialized() && arePropertySpecificationsReadyToRender()
    }

    private fun arePropertySpecificationsReadyToRender():Boolean {
        propertySpecificationList.forEach {
            val ready = it.readyToRender()
            if (!ready) {
                return false
            }
        }
        return true
    }

    /**
     * collection layout needs only to be initialized once with an object (pars pro toto, prototype)
     * obj acts as a kind prototype - we assume all elements in the collection have the same structure
     */
    fun addObject(obj: TObject, aggregator: CollectionAggregator, referrer: String) {
        if (!isInitialized()) {
            // members contain all properties, regardless if hidden, disabled, etc.
            val members = obj.getProperties()
            members.forEach { m ->
                val ps = PropertySpecification(m)
                propertySpecificationList.add(ps)
                val l = m.getInvokeLink()!!
                ResourceProxy().fetch(l, aggregator, referrer = referrer)
            }
        }
    }

    private fun isInitialized(): Boolean {
        return propertySpecificationList.isNotEmpty() && propertySpecificationList.size > 0
    }

    fun addPropertyDetails(propertyBs: PropertyBs) {
        val id = propertyBs.id
        val ps: PropertySpecification = propertySpecificationList.firstOrNull { it.id == id }!!
        ps.amendWith(propertyBs)
    }

    // FIXME NPE -> ISIS-2846 ?
    //FIXME hidden etc is not contained in ObjectProperty, see GidBs for the collection.protoType
    // e.g. http://localhost:9090/restful/objects/demo.JavaLangStringEntity/356/object-layout
    // furthermore, the column header can be taken from there as well

}