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
import org.apache.isis.client.kroviz.core.event.RoXmlHttpRequest
import org.apache.isis.client.kroviz.core.model.ObjectDM
import org.apache.isis.client.kroviz.layout.Layout
import org.apache.isis.client.kroviz.to.HttpError
import org.apache.isis.client.kroviz.to.Property
import org.apache.isis.client.kroviz.to.ResultObject
import org.apache.isis.client.kroviz.to.TObject
import org.apache.isis.client.kroviz.to.bs3.Grid
import org.apache.isis.client.kroviz.ui.ErrorDialog
import org.apache.isis.client.kroviz.ui.kv.Constants
import org.apache.isis.client.kroviz.ui.kv.UiManager

class ObjectAggregator(val actionTitle: String) : BaseAggregator() {

    init {
        dpm = ObjectDM(actionTitle)
    }

    override fun update(logEntry: LogEntry, subType: String) {

        when (val obj = logEntry.getTransferObject()) {
            is TObject -> handleObject(obj)
            is ResultObject -> handleResultObject(obj)
            is Property -> handleProperty(obj)
            is Layout -> handleLayout(obj)
            is Grid -> handleGrid(obj)
            is HttpError -> ErrorDialog(logEntry).open()
            else -> log(logEntry)
        }

        if (dpm.canBeDisplayed()) {
            UiManager.openObjectView(this)
        }
    }

    fun handleObject(obj: TObject) {
        dpm.addData(obj)
        val l = obj.getLayoutLink()!!
        // Json.Layout is invoked first
        RoXmlHttpRequest().invoke(l,this)
        // then Xml.Layout is to be invoked as well
        RoXmlHttpRequest().invoke(l,this, Constants.subTypeXml)
    }

    fun handleResultObject(obj: ResultObject) {
        // TODO dsp.addData(obj)
    }

    override fun getObject(): TObject? {
        return dpm.getObject()
    }

    private fun handleProperty(property: Property) {
        //TODO  yet to be implemented
    }

    private fun handleLayout(layout: Layout) {
        val dm = dpm as ObjectDM
        if (dm.layout == null) {
            dm.addLayout(layout)
            dm.propertyLayoutList.forEach { p ->
                val l = p.link!!
                val isDn = l.href.contains("datanucleus")
                if (isDn) {
                    //invoking DN links leads to an error
                    RoXmlHttpRequest().invoke(l,this)
                }
            }
        }
    }

    private fun handleGrid(grid: Grid) {
        (dpm as ObjectDM).grid = grid
    }

    override fun reset(): ObjectAggregator {
        dpm.isRendered = false
        return this
    }

}
