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
package org.apache.causeway.client.kroviz.core.aggregator

import org.apache.causeway.client.kroviz.core.event.EventState
import org.apache.causeway.client.kroviz.core.event.LogEntry
import org.apache.causeway.client.kroviz.core.event.ResourceProxy
import org.apache.causeway.client.kroviz.core.event.ResourceSpecification
import org.apache.causeway.client.kroviz.core.model.CollectionDM
import org.apache.causeway.client.kroviz.core.model.CollectionLayout
import org.apache.causeway.client.kroviz.to.*
import org.apache.causeway.client.kroviz.ui.core.ViewManager

/** sequence of operations:
 * (0) list
 * (1) FR_OBJECT                TObjectHandler -> invoke()
 * (2) FR_OBJECT_LAYOUT         layoutHandler -> invoke(layout.getProperties()[].getLink()) link can be null?
 * (3) FR_OBJECT_PROPERTY       PropertyHandler -> invoke()
 * (4) FR_PROPERTY_DESCRIPTION  <PropertyDescriptionHandler>
 */
class CollectionAggregator(actionTitle: String, private val parent: ObjectAggregator? = null) : AggregatorWithLayout() {

    init {
        displayModel = CollectionDM(actionTitle)
    }

    var referrer = ""

    override fun update(logEntry: LogEntry, subType: String?) {
        super.update(logEntry, subType)
        if (logEntry.state == EventState.DUPLICATE) {
            throw IllegalStateException("duplicates should not be propagated to handlers")
            //TODO this may not hold true for changed and deleted objects - object version required to deal with it?
        } else {
            referrer = logEntry.url
            when (val obj = logEntry.getTransferObject()) {
                null -> log(logEntry)
                is ResultList -> handleList(obj)
                is TObject -> handleObject(obj)
                is DomainType -> handleDomainType(obj)
                //is GridBs -> Unit //In the case of CollectionDM, object-layout can be ignored
                is Property -> handleProperty(obj)
                is Collection -> handleCollection(obj)
                is Icon -> handleIcon(obj)
                else -> log(logEntry)
            }

            when {
                isStandAloneCollection() && readyToRender() -> {
                    ViewManager.openCollectionView(this)
                }

                isParentedCollection() -> {
                    // A LogEntry with an empty url is passed on to the parent AGGT
                    // in order to decide, if the whole tree is ready to be rendered.
                    val le = LogEntry(ResourceSpecification(""))
                    parent!!.update(le, subType)
                }

                else -> Unit
            }
        }
    }

    private fun readyToRender(): Boolean {
        return getDisplayModel().readyToRender()
    }

    private fun getDisplayModel(): CollectionDM {
        return displayModel as CollectionDM
    }

    private fun getLayout(): CollectionLayout {
        return getDisplayModel().layout as CollectionLayout
    }

    private fun handleList(resultList: ResultList) {
        if (resultList.resulttype != ResultType.VOID.type) {
            val result = resultList.result!!
            result.value.forEach {
                invoke(it, this, referrer = referrer)
            }
        }
    }

    private fun handleObject(obj: TObject) {
        displayModel.addData(obj, this, referrer)

        getLayout().initColumns(obj)

        if (isStandAloneCollection()) {
            invokeLayoutLink(obj, this, referrer = referrer)
        }
        invokeIconLink(obj, this, referrer = referrer)
    }

    private fun isStandAloneCollection(): Boolean {
        return parent == null
    }

    private fun isParentedCollection(): Boolean {
        return parent != null
    }

    private fun handleIcon(obj: TransferObject?) {
        getDisplayModel().addIcon(obj)
    }

    private fun handleDomainType(obj: DomainType) {
        obj.links.forEach {
            if (it.relation() == Relation.LAYOUT) {
                invoke(it, this, referrer = referrer)
            }
        }
        obj.members.forEach {
            val m = it.value
            if (m.isProperty()) {
                invoke(m, this, referrer = referrer)
            }
        }
    }

    private fun handleProperty(property: Property) {
        handleProperty(property, referrer, getLayout())
    }

    private fun handleCollection(collection: Collection) {
        console.log("[CA_handleCollection]")
        if (isParentedCollection()) {
            val cdm = getDisplayModel()
            cdm.id = collection.id
            // add displayModel to parent.displayModel
            val parentDM = parent!!.getDisplayModel()
            parentDM.collectionModelList.add(cdm)
        }
        collection.links.forEach {
            if (it.relation() == Relation.DESCRIBED_BY) {
                ResourceProxy().fetch(it, this, referrer = referrer)
            }
        }
        collection.value.forEach {
            ResourceProxy().fetch(it, this, referrer = referrer)
        }
    }

    override fun reset(): CollectionAggregator {
        displayModel.reset()
        return this
    }

}
