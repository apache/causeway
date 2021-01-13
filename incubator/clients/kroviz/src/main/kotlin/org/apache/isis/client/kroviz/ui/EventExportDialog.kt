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
import org.apache.isis.client.kroviz.core.event.LogEntry
import org.apache.isis.client.kroviz.core.event.ReplayEvent
import org.apache.isis.client.kroviz.to.ValueType
import org.apache.isis.client.kroviz.ui.kv.RoDialog
import pl.treksoft.kvision.core.StringPair
import pl.treksoft.kvision.form.select.SimpleSelect

class EventExportDialog() : Command() {

    private lateinit var form: RoDialog
    private var output: String = ""
    val formItems = mutableListOf<FormItem>()
    val events = mutableListOf<ReplayEvent>()

    private fun collectReplayEvents() {
        EventStore.log.forEach { it ->
            val re = buildExportEvent(it)
            when (it.state) {
                EventState.SUCCESS -> events.add(re)
                EventState.ERROR -> events.add(re)
                else -> {
                }
            }
        }
    }

    override fun execute() {
        val filter = extractUserInput("Filter");
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
        val format = extractUserInput("Format");
        when (format) {
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
        val formPanel = form.formPanel
        val kids = formPanel!!.getChildren()
        //iterate over FormItems (0,1) but not Buttons(2,3)
        for (i in kids) {
            when (i) {
                is SimpleSelect -> {
                    val key = i.label!!
                    val value = i.getValue()!!
                    console.log("[EED.extractUserInput] $key $value")
                    if (key == fieldName) {
                        return value
                    }
                }
            }
        }
        return null
    }

    private fun collectUnfinishedEvents() {
        EventStore.log.forEach { it ->
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
        EventStore.log.forEach { it ->
            val re = buildExportEvent(it)
            events.add(re)
        }
    }

    fun open() {
        console.log("[ExportDialog.open]")
        val format = mutableListOf<StringPair>()
        format.add(StringPair("CSV", "CSV"))
        format.add(StringPair("JSON", "JSON"))
        formItems.add(FormItem("Format", ValueType.SIMPLE_SELECT, format))
        val filter = mutableListOf<StringPair>()
        filter.add(StringPair("NONE", "No Filter"))
        filter.add(StringPair("REPLAY", "For Replay"))
        filter.add(StringPair("UNFINISHED", "Unfinished"))
        formItems.add(FormItem("Filter", ValueType.SIMPLE_SELECT, filter))

        form = RoDialog(caption = "Export", items = formItems, command = this)
        form.open()
    }

    private fun asCsv(events: MutableList<ReplayEvent>): String {
        val DEL = ";"
        val NL = "\n"
        var csv = "URL$DEL STATE$DEL METHOD$DEL REQUEST$DEL START$DEL DURATION$NL"
        events.forEach { e ->
            csv += e.url + DEL
            csv += e.state + DEL
            csv += e.method + DEL
            csv += e.request + DEL
            csv += e.start + DEL
            csv += e.duration.toString() + NL
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
