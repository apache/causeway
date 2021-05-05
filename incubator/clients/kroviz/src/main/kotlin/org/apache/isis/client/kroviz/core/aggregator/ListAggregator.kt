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

import org.apache.isis.client.kroviz.core.event.EventState
import org.apache.isis.client.kroviz.core.event.LogEntry
import org.apache.isis.client.kroviz.core.model.ListDM
import org.apache.isis.client.kroviz.layout.Layout
import org.apache.isis.client.kroviz.to.*
import org.apache.isis.client.kroviz.to.bs3.Grid
import org.apache.isis.client.kroviz.ui.core.UiManager

/** sequence of operations:
 * (0) list
 * (1) FR_OBJECT                TObjectHandler -> invoke()
 * (2) FR_OBJECT_LAYOUT         layoutHandler -> invoke(layout.getProperties()[].getLink()) link can be null?
 * (3) FR_OBJECT_PROPERTY       PropertyHandler -> invoke()
 * (4) FR_PROPERTY_DESCRIPTION  <PropertyDescriptionHandler>
 */
class ListAggregator(actionTitle: String) : AggregatorWithLayout() {

    init {
        dpm = ListDM(actionTitle)
    }

    override fun update(logEntry: LogEntry, subType: String) {

        if (logEntry.state == EventState.DUPLICATE) {
            console.log("[ListAggregator.update] TODO duplicates should not be propagated to handlers")
        } else {
            when (val obj = logEntry.getTransferObject()) {
                null -> log(logEntry)
                is ResultList -> handleList(obj)
                is TObject -> handleObject(obj)
                is Layout -> handleLayout(obj, dpm as ListDM)
                is Grid -> handleGrid(obj)
                is Property -> handleProperty(obj)
                else -> log(logEntry)
            }

            if (dpm.canBeDisplayed()) {
                UiManager.openListView(this)
            }
        }
    }

    private fun handleList(resultList: ResultList) {
        if (resultList.resulttype != ResultType.VOID.type) {
            val result = resultList.result!!
            result.value.forEach {
                invoke(it, this)
            }
        }
    }

    private fun handleObject(obj: TObject) {
        dpm.addData(obj)
        invokeLayoutLink(obj)
    }


    private fun handleGrid(grid: Grid) {
        (dpm as ListDM).grid = grid
    }

    private fun handleProperty(p: Property) {
        val dm = dpm as ListDM
        if (p.isPropertyDescription()) {
            dm.addPropertyDescription(p)
        } else {
            dm.addProperty(p)
            invoke(p.descriptionLink()!!, this)
        }
    }

    override fun reset(): ListAggregator {
        dpm.reset()
        return this
    }

    private fun Property.descriptionLink(): Link? {
        return links.find {
            it.relation() == Relation.DESCRIBED_BY
        }
    }

    private fun Property.isPropertyDescription(): Boolean {
        val selfLink = this.links.find { l ->
            l.relation() == Relation.SELF
        }
        return selfLink!!.representation() == Represention.PROPERTY_DESCRIPTION
    }

}
