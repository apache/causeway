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
package org.apache.isis.client.kroviz.ui

import org.apache.isis.client.kroviz.core.event.EventState
import org.apache.isis.client.kroviz.core.event.EventStore
import org.apache.isis.client.kroviz.core.event.ReplayEvent
import org.apache.isis.client.kroviz.ui.kv.RoDialog
import org.apache.isis.client.kroviz.utils.DomUtil
import org.apache.isis.client.kroviz.utils.Utils

class ExportDialog() :Command() {

    private var jsonOutput: String = ""

    fun open() {
        val replayEvents = collectReplayEvents()
        jsonOutput = JSON.stringify(replayEvents)
        jsonOutput = Utils.format(jsonOutput)
        val formItems = mutableListOf<FormItem>()
        formItems.add(FormItem("JSON", "TextArea", jsonOutput, 15))
        val label = "Download Replay Events"
       RoDialog(caption = label, items = formItems, command = this).open()
    }

    private fun collectReplayEvents(): MutableList<ReplayEvent> {
        val replayEvents = mutableListOf<ReplayEvent>()
       EventStore.log.forEach { it ->
            val re = ReplayEvent(
                    url = it.url,
                    method = it.method!!,
                    request = it.request,
                    state = it.state.toString(),
                    offset = 0L,   // can timimg be relevant ?
                    response = it.response
            )
            when (it.state) {
               EventState.SUCCESS -> replayEvents.add(re)
               EventState.ERROR -> replayEvents.add(re)
                else -> {
                }
            }
        }
        return replayEvents
    }

    override fun execute() {
        DomUtil.download("ReplayEvents.json", jsonOutput)
    }

}
