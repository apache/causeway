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
import org.apache.causeway.client.kroviz.core.model.ObjectDM
import org.apache.causeway.client.kroviz.to.*
import org.apache.causeway.client.kroviz.to.bs.GridBs
import org.apache.causeway.client.kroviz.ui.core.Constants
import org.apache.causeway.client.kroviz.ui.core.ViewManager
import org.apache.causeway.client.kroviz.ui.dialog.ErrorDialog
import org.apache.causeway.client.kroviz.utils.StringUtils

/** sequence of operations:
 * (0) Menu Action              User clicks BasicTypes.String -> handled by ActionDispatcher
 * (1) OBJECT                TObjectHandler -> invoke()   -> passed on to ObjectAggregator
 * (2) OBJECT_LAYOUT         layoutHandler -> invoke(layout.getProperties()[].getLink()) link can be null?
 * (3) ???_OBJECT_PROPERTY       PropertyHandler -> invoke()
 * (4) ???_PROPERTY_DESCRIPTION  <PropertyDescriptionHandler>
 */
class ObjectAggregator(private val actionTitle: String) : AggregatorWithLayout() {
    private var isContainedInParentCollection = false

    private var displayModel = ObjectDM(actionTitle)

    override fun update(logEntry: LogEntry, subType: String?) {
        super.update(logEntry, subType)
        if (logEntry.isUpdatedFromParentedCollection()) {
            isContainedInParentCollection = true
        } else {
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

        if (getDisplayModel().readyToRender()) {
            ViewManager.openObjectView(this)
        }
    }

    private fun handleObject(obj: TObject, referrer: String) {
        // After ~/action/invoke is called, the actual object instance (containing properties) needs to be invoked as well.
        // Note that rel.self/href is identical and both are of type TObject. logEntry.url is different, though.
        if (obj.getProperties().size == 0) {
            invokeInstance(obj, referrer)
        } else {
            displayModel.addData(obj)
        }
        invokeLayoutLink(obj, this, referrer = referrer)
    }

    private fun invokeInstance(obj: TObject, referrer: String) {
        val selfLink = obj.links.find { l ->
            l.relation() == Relation.SELF
        }
        invoke(selfLink!!, this, referrer = referrer)
    }

    fun getDisplayModel(): ObjectDM {
        return displayModel
    }

    private fun handleResultObject(resultObject: ResultObject) {
        getDisplayModel().addResult(resultObject)
    }

    private fun handleResultValue(resultValue: ResultValue) {
        throw NotImplementedError("$resultValue to be handled")
    }

    override fun getObject(): TObject {
        return displayModel.getObject()
    }

    private fun handleProperty(p: Property, referrer: String) {
        if (!p.isPropertyDescription()) {
            val pdl = p.getDescriptionLink() ?: return
            invoke(pdl, this, referrer = referrer)
        }
    }

    private fun handleGrid(grid: GridBs, referrer: String) {
        getDisplayModel().addLayout(grid, this, referrer)
        grid.getPropertyList().forEach {
            val link = it.link!!
            invoke(link, this, subType = Constants.subTypeJson, referrer = referrer)
        }
    }

    fun getTitle(): String {
        var title: String = StringUtils.extractTitle(getDisplayModel().title)
        if (title.isEmpty()) {
            title = actionTitle
        }
        return title
    }

    override fun reset(): ObjectAggregator {
        displayModel.reset()
        return this
    }

    /**
     * This is done in order to have the parent check, if itself and it's children can be displayed
     */
    private fun LogEntry.isUpdatedFromParentedCollection(): Boolean {
        return this.url == ""
    }

}