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

import org.apache.causeway.client.kroviz.to.ObjectProperty
import org.apache.causeway.client.kroviz.to.PropertyDescription

/**
 * Properties have multiple aspects:
 *
 * - Member of a DomainObject
 * - Description (friendlyName, etc.)
 * - Layout (disabledReason, labelPosition, etc.)
 * - Visibility (hidden)
 *
 * All are required in order to be correctly displayed (in a table).
 */
class PropertySpecification(val id: String) {
    var name = "" // aka: columnName, named, label
    var hidden = false
    var disabledReason = ""

    fun addObjectProperty(op: ObjectProperty) {}
    fun addPropertyDescription(pd: PropertyDescription) {
        val ex = pd.extensions!!
        name = ex.getFriendlyName()
        if (name.isEmpty()) {
            name = id
            console.log(pd)
        }
    }
    //FIXME use setter Methods using:
    /** Property
     *ObjectProperty
    PropertyDescription */
    /*init {
        id = grid.id
        when {
            grid.named.isNotEmpty() -> columnName = grid.named
            else -> columnName = id
        }
        //       console.log("[CD.init]")
        when {
            // grid.hidden can be null -- intellij inference is wrong
            (grid.hidden == null) || grid.hidden.isEmpty() -> {}
            grid.hidden.contains("TABLE") -> hidden = true
        }
        //       console.log("$id $columnName Hidden: $hidden")
    } */

}