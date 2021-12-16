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
package org.apache.isis.client.kroviz.handler

import kotlinx.serialization.json.Json
import org.apache.isis.client.kroviz.core.aggregator.DomainTypesAggregator
import org.apache.isis.client.kroviz.to.DomainTypes
import org.apache.isis.client.kroviz.to.TransferObject
import org.apache.isis.client.kroviz.ui.core.ViewManager

class DomainTypesHandler : BaseHandler() {

    override fun parse(response: String): TransferObject {
        return Json.decodeFromString(DomainTypes.serializer(), response)
    }

    override fun doHandle() {
        if (ViewManager.loadDomainTypes()) {
            //setting the Aggregator leads to cascading loads of all Domaintypes (~ 1500 requests for Demo)
            val url = logEntry.url
            logEntry.addAggregator(DomainTypesAggregator(url))
            update()
        }
    }

}
