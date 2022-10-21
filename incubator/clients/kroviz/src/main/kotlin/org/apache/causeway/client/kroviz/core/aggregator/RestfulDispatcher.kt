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

import org.apache.causeway.client.kroviz.core.event.CorsHttpRequest
import org.apache.causeway.client.kroviz.core.event.LogEntry
import org.apache.causeway.client.kroviz.to.Link
import org.apache.causeway.client.kroviz.to.Relation
import org.apache.causeway.client.kroviz.to.Restful
import org.apache.causeway.client.kroviz.ui.core.SessionManager

class RestfulDispatcher() : BaseAggregator() {

    override fun update(logEntry: LogEntry, subType: String) {
        val restful = logEntry.getTransferObject() as Restful
        restful.links.forEach {
            val rel = it.rel
            when {
                rel.endsWith(Relation.SELF.name) -> {}
                rel.endsWith("/menuBars") -> invokeNavigation(it)
                rel.endsWith("/services") -> {}
                rel.endsWith("/logout") -> {}
                rel.endsWith("/brand-logo-signin") -> invokeDisgustingCorsWorkaround(it)
                rel.endsWith("/brand-logo-header") -> invokeDisgustingCorsWorkaround(it)
                else -> invokeSystem(it)
            }
        }
    }

    private fun invokeNavigation(link: Link) {
        invoke(link, NavigationDispatcher(), referrer = "")
    }

    private fun invokeSystem(link: Link) {
        invoke(link, SystemAggregator(), referrer = "")
    }

    private fun invokeDisgustingCorsWorkaround(link: Link) {
        val credentials = SessionManager.getCredentials()!!
        CorsHttpRequest().invoke(link.href, credentials)
    }

}
