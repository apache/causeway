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

import org.apache.causeway.client.kroviz.core.event.LogEntry
import org.apache.causeway.client.kroviz.core.event.ResourceProxy
import org.apache.causeway.client.kroviz.core.model.ObjectDM
import org.apache.causeway.client.kroviz.core.model.ObjectLayout
import org.apache.causeway.client.kroviz.to.*
import org.apache.causeway.client.kroviz.to.bs.GridBs
import org.apache.causeway.client.kroviz.ui.core.Constants
import org.apache.causeway.client.kroviz.ui.core.ViewManager
import org.apache.causeway.client.kroviz.ui.dialog.ErrorDialog

/** sequence of operations:
 * (0) Menu Action              User clicks BasicTypes.String -> handled by ActionDispatcher
 * (1) OBJECT                TObjectHandler -> invoke()   -> passed on to ObjectAggregator
 * (2) OBJECT_LAYOUT         layoutHandler -> invoke(layout.getProperties()[].getLink()) link can be null?
 * (3) ???_OBJECT_PROPERTY       PropertyHandler -> invoke()
 * (4) ???_PROPERTY_DESCRIPTION  <PropertyDescriptionHandler>
 */
class ObjectAggregator(val actionTitle: String) : AggregatorWithLayout() {

    private var collectionMap = mutableMapOf<String, CollectionAggregator>()

    init {
        displayModel = ObjectDM(actionTitle)
    }

    override fun update(logEntry: LogEntry, subType: String?) {
        super.update(logEntry, subType)
        if (!logEntry.isUpdatedFromParentedCollection()) {
            val referrer = logEntry.url
            when (val obj = logEntry.getTransferObject()) {
                is TObject -> handleObject(obj, referrer)
                is ResultObject -> handleResultObject(obj)
                is ResultValue -> handleResultValue(obj)
                is Property -> handleProperty(obj, referrer)
                is GridBs -> handleGrid(obj, referrer)
                is HttpError -> ErrorDialog(logEntry).open()
                else -> log(logEntry)
            }
        }

        if (displayModel.readyToRender()) {
            ViewManager.openObjectView(this)
        }
    }

    private fun handleObject(obj: TObject, referrer: String) {
        // After ~/action/invoke is called, the actual object instance (containing properties) needs to be invoked as well.
        // Note that rel.self/href is identical and both are of type TObject. logEntry.url is different, though.
        if (obj.getProperties().size == 0) {
            invokeInstance(obj, referrer)
        } else {
            displayModel.addData(obj, this, referrer)
        }
        if (collectionMap.isEmpty()) {
            handleCollections(obj, referrer)
        }
        invokeLayoutLink(obj, this, referrer = referrer)
    }

    private fun invokeInstance(obj: TObject, referrer: String) {
        val selfLink = obj.links.find { l ->
            l.relation() == Relation.SELF
        }
        invoke(selfLink!!, this, referrer = referrer)
    }

    private fun handleResultObject(resultObject: ResultObject) {
        (displayModel as ObjectDM).addResult(resultObject)
    }

    private fun handleResultValue(resultValue: ResultValue) {
        throw NotImplementedError("$resultValue to be handled")
    }

    override fun getObject(): TObject? {
        return displayModel.getObject()
    }

    private fun handleCollections(obj: TObject, referrer: String) {
        console.log("[OA_handleCollection] collections")
        val collections = obj.getCollections()
        console.log(collections)
        collections.forEach {
            val key = it.id
            val aggregator = CollectionAggregator(key, this)
            collectionMap[key] = aggregator
            val link = it.links.first()
            ResourceProxy().fetch(link, aggregator, referrer = referrer)
        }
    }

    private fun handleProperty(property: Property, referrer: String) {
        val dm = displayModel as ObjectDM
        val layout = dm.layout!!
        handleProperty(property, referrer, layout)
    }

    private fun handleGrid(grid: GridBs, referrer: String) {
        val odm = displayModel as ObjectDM
        val ol = odm.layout as ObjectLayout
        // for a yet unknown reason, handleGrid may be called twice, therefore we check if it's already set
        if (ol.grid == null) {
            console.log("[OA_handleGrid]")
            ol.addGrid(grid, this, referrer = referrer)
            val pl = grid.getPropertyList()
            pl.forEach {
                val link = it.link!!
                // properties to be handled by ObjectAggregator
                ResourceProxy().fetch(link, this, subType = Constants.subTypeJson, referrer = referrer)
            }
            val cl = grid.getCollectionList()
            cl.forEach {
                val href = it.linkList.first().href
                console.log("CollectionBs")
                console.log(href)
                val l = Link(href = href)
                // collections to be handled by ObjectAggregator
                ResourceProxy().fetch(l, this, subType = Constants.subTypeJson, referrer = referrer)
            }
        }
    }

    override fun reset(): ObjectAggregator {
        displayModel.reset()
        return this
    }

    /**
     * This is done in order to have the parent check, if it and it's children can be displayed
     */
    private fun LogEntry.isUpdatedFromParentedCollection(): Boolean {
        return this.url == ""
    }

}
