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

import org.apache.causeway.client.kroviz.to.Member

class CollectionLayout {
    var propertyDetailsList = mutableListOf<PropertyDetails>()

    fun getPropertySpecification(id: String): PropertyDetails {
        return propertyDetailsList.firstOrNull { it.id == id }!!
    }

    fun readyToRender(): Boolean {
        return isInitialized() && arePropertySpecificationsReadyToRender()
    }

    private fun arePropertySpecificationsReadyToRender(): Boolean {
        propertyDetailsList.forEach {
            val ready = it.readyToRender()
            if (!ready) {
                return false
            }
        }
        return true
    }

    fun addMember(m: Member) {
        val ps = PropertyDetails(m)
        propertyDetailsList.add(ps)
    }

    fun isInitialized(): Boolean {
        return propertyDetailsList.isNotEmpty() && propertyDetailsList.size > 0
    }

}
