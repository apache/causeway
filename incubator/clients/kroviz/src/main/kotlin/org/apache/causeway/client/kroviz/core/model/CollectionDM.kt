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

import io.kvision.state.observableListOf
import org.apache.causeway.client.kroviz.to.PropertyDescription
import org.apache.causeway.client.kroviz.to.TObject
import org.apache.causeway.client.kroviz.to.TransferObject
import org.apache.causeway.client.kroviz.to.bs.GridBs
import org.apache.causeway.client.kroviz.to.bs.PropertyBs

class CollectionDM(override var title: String) : DisplayModelWithLayout() {
    val collectionLayout = CollectionLayout()

    var id = ""
    var data = observableListOf<Exposer>()
    private var rawData = observableListOf<TransferObject>()
    private var protoType: TObject? = null
    private var protoTypeLayout: GridBs? = null

    fun getTitle(): String {
        return title
    }

    fun setProtoTypeLayout(grid: GridBs) {
        protoTypeLayout = grid
        val propertyList = grid.getPropertyList()
        propertyList.forEach {
            addPropertyDetails(it)
        }
    }

    private fun addPropertyDetails(propertyBs: PropertyBs) {
        val id = propertyBs.id
        val ps = collectionLayout.getPropertySpecification(id)
        ps.amendWith(propertyBs)
    }

    fun addPropertyDescription(propertyDescription: PropertyDescription) {
        val id = propertyDescription.id
        val ps = collectionLayout.getPropertySpecification(id)
        ps.amendWith(propertyDescription)
    }

    fun hasProtoType(): Boolean {
        return protoType != null
    }

    fun setProtoType(to: TransferObject) {
        protoType = to as TObject
    }

    override fun readyToRender(): Boolean {
        return collectionLayout.readyToRender()
    }

    override fun addData(obj: TransferObject) {
        rawData.add(obj)
        val exo = Exposer(obj as TObject)
        //if exposer is not dynamised, data access in Tabulator tables won't work
        data.add(exo.dynamise() as Exposer)
    }

    override fun reset() {
        isRendered = false
        data = observableListOf()
        rawData = observableListOf()
    }

}
