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
package org.apache.isis.client.kroviz.core.model

import io.kvision.state.observableListOf
import org.apache.isis.client.kroviz.to.TObject
import org.apache.isis.client.kroviz.to.TransferObject

class CollectionDM(override val title: String) : DisplayModelWithLayout() {
    var data = observableListOf<Exposer>()
    private var rawData = observableListOf<TransferObject>()

    override fun addData(obj: TransferObject) {
        if (!rawData.contains(obj)) {
            rawData.add(obj)
            val exo = Exposer(obj as TObject)
            data.add(exo.dynamise())  //if exposer is not dynamised, data access in tables won't work
        }
    }

    fun addIcon(obj: TransferObject?) {
        TODO("Not yet implemented")
    }

    override fun reset() {
        isRendered = false
        data = observableListOf()
        rawData = observableListOf()
    }

}
