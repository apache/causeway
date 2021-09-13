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
import org.apache.isis.client.kroviz.to.Link
import org.apache.isis.client.kroviz.to.Relation
import org.apache.isis.client.kroviz.to.Restful

class RestfulDispatcher() : BaseAggregator() {

    override fun update(logEntry: LogEntry, subType: String) {
        val restful = logEntry.getTransferObject() as Restful
        restful.links.forEach {
            when {
                it.rel.endsWith(Relation.SELF.name) -> { }
                it.rel.endsWith("/menuBars") -> invokeNavigation(it)
                it.rel.endsWith("/services") -> {
                }
/*                it.rel.endsWith("/brand-logo-signin") -> {
                }
                it.rel.endsWith("/brand-logo-header") -> {
                }   */
                else -> invokeSystem(it)
            }
        }
    }

    private fun invokeNavigation(it: Link) {
        invoke(it, NavigationDispatcher(), referrer = "")
    }

    private fun invokeSystem(it: Link) {
        invoke(it, SystemAggregator(), referrer = "")
    }

}
