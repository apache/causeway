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
package org.apache.isis.client.kroviz.core.aggregator

import org.apache.isis.client.kroviz.core.event.LogEntry
import org.apache.isis.client.kroviz.core.model.ObjectDM
import org.apache.isis.client.kroviz.layout.Layout
import org.apache.isis.client.kroviz.to.*
import org.apache.isis.client.kroviz.to.bs3.Grid
import org.apache.isis.client.kroviz.ui.ErrorDialog
import org.apache.isis.client.kroviz.ui.kv.UiManager

/** sequence of operations:
 * (0) Menu Action              User clicks BasicTypes.String -> handled by ActionDispatcher
 * (1) OBJECT                TObjectHandler -> invoke()   -> passed on to ObjectAggregator
 * (2) OBJECT_LAYOUT         layoutHandler -> invoke(layout.getProperties()[].getLink()) link can be null?
 * (3) ???_OBJECT_PROPERTY       PropertyHandler -> invoke()
 * (4) ???_PROPERTY_DESCRIPTION  <PropertyDescriptionHandler>
 */
class ObjectAggregator(val actionTitle: String) : AggregatorWithLayout() {

    init {
        dpm = ObjectDM(actionTitle)
    }

    override fun update(logEntry: LogEntry, subType: String) {
        val obj = logEntry.getTransferObject()
        when (obj) {
            is TObject -> handleObject(obj)
            is ResultObject -> handleResultObject(obj)
            is Property -> handleProperty(obj)
            is Layout -> handleLayout(obj, dpm as ObjectDM)
            is Grid -> handleGrid(obj)
            is HttpError -> ErrorDialog(logEntry).open()
            else -> log(logEntry)
        }

        if (dpm.canBeDisplayed()) {
            UiManager.openObjectView(this)
        }
    }

    fun handleObject(obj: TObject) {
        // After ~/action/invoke is called, the actual object instance (containing properties) needs to be invoked as well.
        // Note that rel.self/href is identical in both cases and both are of type TObject. logEntry.url is different, though.
        if (obj.getProperties().size == 0) {
            invokeInstance(obj)
        } else {
            dpm.addData(obj)
        }
        invokeLayoutLink(obj)
    }

    private fun invokeInstance(obj: TObject) {
        val selfLink = obj.links.find { l ->
            l.relation() == Relation.SELF
        }
        invoke(selfLink!!, this)
    }

    fun handleResultObject(obj: ResultObject) {
        console.log("[OA.handleResultObject] TODO implement")
        console.log(obj)
    }

    override fun getObject(): TObject? {
        return dpm.getObject()
    }

    private fun handleProperty(property: Property) {
        console.log("[OA.handleProperty] TODO implement")
        console.log(property)
    }

    private fun handleGrid(grid: Grid) {
        (dpm as ObjectDM).grid = grid
    }

    override fun reset(): ObjectAggregator {
        dpm.isRendered = false
        return this
    }

}
