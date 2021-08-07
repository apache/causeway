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
import org.apache.isis.client.kroviz.core.event.RoXmlHttpRequest
import org.apache.isis.client.kroviz.core.model.CollectionDM
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
class CollectionAggregator(actionTitle: String, val parent: ObjectAggregator? = null) : AggregatorWithLayout() {

    init {
        dpm = CollectionDM(actionTitle)
    }

    override fun update(logEntry: LogEntry, subType: String) {

        if (logEntry.state == EventState.DUPLICATE) {
            console.log("[CollectionAggregator.update] TODO duplicates should not be propagated to handlers")
        } else {
            when (val obj = logEntry.getTransferObject()) {
                null -> log(logEntry)
                is ResultList -> handleList(obj)
                is TObject -> handleObject(obj)
                is DomainType -> handleDomainType(obj)
                is Layout -> handleLayout(obj, dpm as CollectionDM)
                is Grid -> handleGrid(obj)
                is Property -> handleProperty(obj)
                is Collection -> handleCollection(obj)
                is Icon -> handleIcon(obj)
                else -> log(logEntry)
            }

            if (parent == null) {
                if (dpm.canBeDisplayed()) {
                    UiManager.openCollectionView(this)
                }
            } else {
                val le = LogEntry("")
                parent.update(le, subType)
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
        invokeLayoutLink(obj, this)
        invokeIconLink(obj, this)
    }

    private fun handleIcon(obj: TransferObject?) {
        (dpm as CollectionDM).addIcon(obj)
    }

    private fun handleDomainType(obj: DomainType) {
        obj.links.forEach {
            if (it.relation() == Relation.LAYOUT) {
                invoke(it, this)
            }
        }
        obj.members.forEach {
            val m = it.value
            if (m.isProperty()) {
                invoke(m, this)
            }
        }
    }

    private fun handleGrid(grid: Grid) {
        (dpm as CollectionDM).grid = grid
    }

    private fun handleProperty(p: Property) {
        val dm = dpm as CollectionDM
        if (p.isPropertyDescription()) {
            dm.addPropertyDescription(p)
        } else {
            dm.addProperty(p)
            val pdl = p.descriptionLink()
            if (pdl != null) {
                invoke(pdl, this)
            }
        }
    }

    private fun handleCollection(collection: Collection) {
        collection.links.forEach {
            if (it.relation() == Relation.DESCRIBED_BY) {
                RoXmlHttpRequest().invoke(it, this)
            }
        }
        collection.value.forEach {
            RoXmlHttpRequest().invoke(it, this)
        }
    }

    override fun reset(): CollectionAggregator {
        dpm.reset()
        return this
    }

    private fun Property.descriptionLink(): Link? {
        return links.find {
            it.relation() == Relation.ELEMENT_TYPE
        }
    }

    private fun Property.isPropertyDescription(): Boolean {
        val selfLink = this.links.find {
            it.relation() == Relation.SELF
        }
        return selfLink!!.representation() == Represention.PROPERTY_DESCRIPTION
    }

}
