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
import org.apache.causeway.client.kroviz.core.model.SystemDM
import org.apache.causeway.client.kroviz.to.DomainTypes
import org.apache.causeway.client.kroviz.to.User
import org.apache.causeway.client.kroviz.to.Version
import org.apache.causeway.client.kroviz.ui.core.SessionManager
import org.apache.causeway.client.kroviz.ui.core.ViewManager
import org.apache.causeway.client.kroviz.utils.ImageUtils
import org.apache.causeway.client.kroviz.utils.UrlUtils

class SystemAggregator : BaseAggregator() {

    init {
        displayModel = SystemDM("not filled (yet)")
    }

    override fun update(logEntry: LogEntry, subType: String?) {
        when (val obj = logEntry.getTransferObject()) {
            is User -> displayModel.addData(obj)
            is Version -> displayModel.addData(obj)
            is DomainTypes -> displayModel.addData(obj)
            else -> {
                if (logEntry.blob != null) {
                    val icon = ImageUtils.extractIcon(logEntry)
                    val url = logEntry.url
                    when (UrlUtils.isApplicationIcon(url)) {
                        url.contains("48") -> {
                            (displayModel as SystemDM).addSmallIcon(icon)
                            val iconUrl = icon.image.src
                            SessionManager.setApplicationIcon(iconUrl)
                        }

                        url.contains("256") -> (displayModel as SystemDM).addLargeIcon(icon)
                        else -> log(logEntry)
                    }
                } else {
                    console.log("[SA.update] blob/image is null")
                }
            }
        }

        if (displayModel.readyToRender()) {
            ViewManager.openObjectView(this)
        }
    }

    override fun reset(): SystemAggregator {
        displayModel.isRendered = false
        return this
    }

}
