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
import org.apache.isis.client.kroviz.core.event.ResourceSpecification
import org.apache.isis.client.kroviz.to.HttpErrorResponse
import org.apache.isis.client.kroviz.ui.core.SessionManager
import org.apache.isis.client.kroviz.ui.dialog.ErrorDialog

class ErrorDispatcher : BaseAggregator() {

    override fun update(logEntry: LogEntry, subType: String) {
        val error = logEntry.getTransferObject() as HttpErrorResponse
        val url = logEntry.url
        val message = error.getMessage()
        val reSpec = ResourceSpecification(url)
        SessionManager.getEventStore().fault(reSpec, message)
        ErrorDialog(logEntry).open()
    }

}
