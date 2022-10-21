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
import org.apache.causeway.client.kroviz.ui.panel.SvgPanel
import org.apache.causeway.client.kroviz.utils.DomUtil
import org.apache.causeway.client.kroviz.utils.UUID

class SvgDispatcher(val callBack: Any) : BaseAggregator() {

    override fun update(logEntry: LogEntry, subType: String) {
        val response = logEntry.response
        when (callBack) {
            is UUID -> DomUtil.appendTo(callBack, response)
//TODO            is SvgPanel -> callBack.renderSvg(response)
            else -> {
            }
        }
    }

}
