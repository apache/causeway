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
package org.apache.isis.client.kroviz.ui.dialog

import org.apache.isis.client.kroviz.core.event.EventState
import org.apache.isis.client.kroviz.core.event.LogEntry
import org.apache.isis.client.kroviz.core.event.ReplayEvent
import org.apache.isis.client.kroviz.to.ValueType
import org.apache.isis.client.kroviz.ui.core.RoDialog
import io.kvision.core.StringPair
import io.kvision.form.select.SimpleSelect
import org.apache.isis.client.kroviz.ui.core.FormItem
import org.apache.isis.client.kroviz.ui.core.SessionManager
import org.apache.isis.client.kroviz.ui.core.UiManager

class EventExportDialog : Controller() {

    private var output: String = ""
    val formItems = mutableListOf<FormItem>()
    val events = mutableListOf<ReplayEvent>()

    private fun collectReplayEvents() {
        SessionManager.getEventStore().log.forEach {
            val re = buildExportEvent(it)
            when (it.state) {
                EventState.SUCCESS_JS -> events.add(re)
                EventState.SUCCESS_XML -> events.add(re)
                EventState.ERROR -> events.add(re)
                else -> {
                }
            }
        }
    }

    override fun execute(action:String?) {
        val filter = extractUserInput("Filter")
        var fileName = ""
        when (filter) {
            "NONE" -> {
                collectAllEvents()
                fileName += "AllEvents"
            }
            "REPLAY" -> {
                collectReplayEvents()
                fileName += "ReplayEvents"
            }
            "UNFINISHED" -> {
                collectUnfinishedEvents()
                fileName += "UnfinishedEvents"
            }
            else -> {
            }
        }
        when (extractUserInput("Format")) {
            "CSV" -> {
                output = asCsv(events)
                fileName += ".csv"
            }
            "JSON" -> {
                output = JSON.stringify(events)
                fileName += ".json"
            }
            else -> {
            }
        }
        DownloadDialog(fileName, output).open()
    }

    private fun extractUserInput(fieldName: String): String? {
        val formPanel = dialog.formPanel
        val kids = formPanel!!.getChildren()
        //iterate over FormItems (0,1) but not Buttons(2,3)
        for (i in kids) {
            when (i) {
                is SimpleSelect -> {
                    val key = i.label!!
                    val value = i.getValue()!!
                    if (key == fieldName) {
                        return value
                    }
                }
            }
        }
        return null
    }

    private fun collectUnfinishedEvents() {
        SessionManager.getEventStore().log.forEach {
            val re = buildExportEvent(it)
            when (it.state) {
                EventState.RUNNING -> events.add(re)
                EventState.ERROR -> events.add(re)
                else -> {
                }
            }
        }
    }

    private fun collectAllEvents() {
        SessionManager.getEventStore().log.forEach {
            val re = buildExportEvent(it)
            events.add(re)
        }
    }

    override fun open() {
        val format = mutableListOf<StringPair>()
        format.add(StringPair("CSV", "CSV"))
        format.add(StringPair("JSON", "JSON"))
        formItems.add(FormItem("Format", ValueType.SIMPLE_SELECT, format))
        val filter = mutableListOf<StringPair>()
        filter.add(StringPair("NONE", "No Filter"))
        filter.add(StringPair("REPLAY", "For Replay"))
        filter.add(StringPair("UNFINISHED", "Unfinished"))
        formItems.add(FormItem("Filter", ValueType.SIMPLE_SELECT, filter))

        dialog = RoDialog(caption = "Export", items = formItems, controller = this)
        super.open()
    }

    private fun asCsv(events: MutableList<ReplayEvent>): String {
        val del = ";"
        val nl = "\n"
        var csv = "URL$del STATE$del METHOD$del REQUEST$del START$del DURATION$nl"
        events.forEach { e ->
            csv += e.url + del
            csv += e.state + del
            csv += e.method + del
            csv += e.request + del
            csv += e.start + del
            csv += e.duration.toString() + nl
        }
        return csv
    }

    private fun buildExportEvent(logEntry: LogEntry): ReplayEvent {
        return ReplayEvent(
                url = logEntry.url,
                method = logEntry.method!!,
                request = logEntry.request,
                state = logEntry.state.toString(),
                start = logEntry.createdAt.toISOString() ,
                duration = logEntry.duration,
                response = logEntry.response
        )
    }

}
