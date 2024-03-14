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
package org.apache.causeway.client.kroviz.handler

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.apache.causeway.client.kroviz.core.aggregator.ActionDispatcher
import org.apache.causeway.client.kroviz.to.Action
import org.apache.causeway.client.kroviz.to.TransferObject

class ActionHandler : BaseHandler() {

    override fun parse(response: String): TransferObject {
        // due to some ambiguity in members, collection-description is accepted as action
        if (response.contains("collection-description")) {
            throw RuntimeException()
        }
        return Json.decodeFromString<Action>(response)
    }

    override fun doHandle() {
        logEntry.addAggregator(ActionDispatcher())
        update()
    }

}

