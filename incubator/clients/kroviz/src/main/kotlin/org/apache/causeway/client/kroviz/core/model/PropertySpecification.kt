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
class PropertySpecification(member: Member) {
    var id = ""
    var name = "" // aka: columnName, named, label, title
    var hidden = false
    var disabled = false
    var isPropertyDescriptionProcessed = false
    var isObjectPropertyProcessed = true //FIXME

    init {
        id = member.id
        name = member.id // can be changed later via property-description
        hidden = false // can be changed later via ...
        //FIXME
        if (name == "sources" || name == "description" || name == "logicalTypeName") {
            hidden = true
        }
        disabled = member.disabledReason.isNotEmpty()
    }

    fun amendWith(op: ObjectProperty) {
        console.log("[PS_amendWith] ObjectProperty")
        //TODO
    }

    fun amendWith(pd: PropertyDescription) {
        console.log("[PS_amendWith] PropertyDescription")
        val ex = pd.extensions!!
        val fn = ex.getFriendlyName()
        if (fn.isNotEmpty()) {
            name = fn
        }
        isPropertyDescriptionProcessed = true
        console.log(this)
    }

    fun readyToRender(): Boolean {
        return isObjectPropertyProcessed && isPropertyDescriptionProcessed
    }

}