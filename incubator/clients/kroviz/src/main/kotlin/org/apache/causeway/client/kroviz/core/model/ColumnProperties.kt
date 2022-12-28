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
import org.apache.causeway.client.kroviz.to.Property
import org.apache.causeway.client.kroviz.to.bs.PropertyBs

/**
 * Properties have three aspects:
 *
 * - Member of a DomainObject
 * - Description (friendlyName, etc.)
 * - Layout (hidden, labelPosition, etc.)
 *
 * All three are required in order to display correctly in a table.
 */
class ColumnProperties(val key: String) {
    var property: Property? = null
    var friendlyName: String = ""
    var layout: PropertyLt? = null
    var grid: PropertyBs? = null
    var hidden: Boolean = false

    fun debug(): String {
        return "property: " + property?.id
    }

    fun initLayout(layout: PropertyLt) {
        this.layout = layout
        console.log("[CP.initLayout]")

        hidden = (layout.hidden != null)
        // properties without labelPosition will be hidden - is that correct?
        // example: Demo -> Strings -> Description
        if (!hidden && layout.labelPosition == null) {
            hidden = true
        }
    }

    fun initGrid(grid: PropertyBs) {
        this.grid = grid
        console.log("[CP.initGrid]")
//FIXME        hidden = (property.hidden != null)
        // properties without labelPosition will be hidden - is that correct?
        // example: Demo -> Strings -> Description
        //  if (!hidden && property.labelPosition == null) {
        //     hidden = true
        // }
    }

}