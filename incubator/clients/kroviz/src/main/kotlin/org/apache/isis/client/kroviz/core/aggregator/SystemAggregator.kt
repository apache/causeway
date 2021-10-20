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
import org.apache.isis.client.kroviz.core.model.SystemDM
import org.apache.isis.client.kroviz.to.DomainTypes
import org.apache.isis.client.kroviz.to.User
import org.apache.isis.client.kroviz.to.Version
import org.apache.isis.client.kroviz.utils.ImageUtils
import org.apache.isis.client.kroviz.utils.UrlUtils

class SystemAggregator() : BaseAggregator() {

    init {
        dpm = SystemDM("not filled (yet)")
    }

    override fun update(logEntry: LogEntry, subType: String) {

        when (val obj = logEntry.getTransferObject()) {
            is User -> dpm.addData(obj)
            is Version -> dpm.addData(obj)
            is DomainTypes -> dpm.addData(obj)
            else -> {
                if (logEntry.blob != null) {
                    val icon = ImageUtils.extractIcon(logEntry)
                    val url = logEntry.url
                    val isApplicationIcon = UrlUtils.isApplicationIcon(url)
                    when (isApplicationIcon) {
                        url.contains("48") -> (dpm as SystemDM).addSmallIcon(icon)
                        url.contains("256") -> (dpm as SystemDM).addLargeIcon(icon)
                        else -> log(logEntry)
                    }
                } else {
                    console.log("[SA.update] TODO ISIS-2768 no blob/image due to CORS")
                }
            }
        }

        if (dpm.canBeDisplayed()) {
//  TODO          UiManager.openObjectView(this)
        }
    }

    override fun reset(): SystemAggregator {
        dpm.isRendered = false
        return this
    }

}
