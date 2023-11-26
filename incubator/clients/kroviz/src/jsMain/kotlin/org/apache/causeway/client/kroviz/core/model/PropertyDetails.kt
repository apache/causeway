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
import org.apache.causeway.client.kroviz.to.PropertyDescription
import org.apache.causeway.client.kroviz.to.TypeMapper
import org.apache.causeway.client.kroviz.to.ValueType
import org.apache.causeway.client.kroviz.to.bs.PropertyBs

/**
 * Aggregate information for each column in order to display in a table, namely:
 * - columnName :   column header/label
 * - id :           attribute to be used to retrieve the value of each cell (id)
 * - hidden :       will the column be displayed or not
 */
class PropertyDetails(member: Member) {
    var id = member.id
    var name = "" // aka: columnName, named, label, title
    var hidden = true
    var disabled = member.disabledReason.isNotEmpty()
    private var isAmendedFromBs = false
    private var isAmendedFromPropertyDescription = false
    var typicalLength: Int = 10
    var type = TypeMapper.match(member)

    fun amendWith(pbs: PropertyBs) {
        name = pbs.named
        //static analysis pretends pbs.hidden can't be null, but runtime proves the opposite
        hidden = !(pbs.hidden != null && pbs.hidden.isNotEmpty())
        //This is hacky
        if (id == "sources") {
            hidden = true
        }
        //static analysis pretends pbs.typicalLength can't be null, but runtime proves the opposite
        if (pbs.typicalLength != null && pbs.typicalLength > 0) {
            typicalLength = pbs.typicalLength
        }
        isAmendedFromBs = true
    }

    fun amendWith(pd: PropertyDescription) {
        val ex = pd.extensions!!
        val fn = ex.getFriendlyName()
        if (fn.isNotEmpty()) {
            name = fn
        }
        isAmendedFromPropertyDescription = true
    }

    fun readyToRender(): Boolean {
        return when (id) {
            "logicalTypeName" -> true
            "objectIdentifier" -> true
            else -> isAmendedFromBs && isAmendedFromPropertyDescription
        }
    }

}