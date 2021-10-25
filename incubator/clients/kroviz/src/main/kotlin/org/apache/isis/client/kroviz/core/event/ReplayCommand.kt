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
package org.apache.isis.client.kroviz.core.event

import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.apache.isis.client.kroviz.main
import org.apache.isis.client.kroviz.to.Link
import org.apache.isis.client.kroviz.to.Represention
import org.apache.isis.client.kroviz.to.TObject
import org.apache.isis.client.kroviz.ui.core.UiManager
import org.apache.isis.client.kroviz.ui.dialog.ReplayDiffDialog
import org.apache.isis.client.kroviz.ui.panel.EventLogTable

val AppScope = CoroutineScope(window.asCoroutineDispatcher())

class ReplayCommand {
    private val eventStore = UiManager.getEventStore()

    fun execute() {
        val expectedEvents = copyEvents(eventStore.log)
        eventStore.reset()
        main() // re-creates the UI, but keeps the UiManager(singleton/object) and the session

        val uiEvents = filterReplayEvents(expectedEvents)
        replay(uiEvents)
        val actualEvents: MutableList<LogEntry> = eventStore.log

        val expectedTable = EventLogTable(expectedEvents)
        val actualTable = EventLogTable(actualEvents)
        val rdd = ReplayDiffDialog()
        rdd.expectedPanel.add(expectedTable)
        rdd.actualPanel.add(actualTable)
        rdd.dialog.open()
    }

    private fun replay(userActions: List<LogEntry>) {
        var previous: LogEntry? = null
        userActions.forEach {
            if (it.isUserAction() && previous != null) {
                val ms = calculateDelay(previous!!, it) // needs to be outside of launch{} - or current=previous
                val obj = it.obj as TObject
                AppScope.launch {
                    delay(ms) // non-blocking delay
                    ResourceProxy().load(obj)
                }
            } else {
                val link = Link(href = it.url)
                ResourceProxy().fetch(link, null, it.subType)
            }
            previous = it
        }
    }

    private fun calculateDelay(previous: LogEntry, current: LogEntry): Long {
        val currentMs = current.createdAt.getTime()
        val previousMs = previous.createdAt.getTime()
        return  currentMs.minus(previousMs).toLong()
    }

    private fun filterReplayEvents(events: List<LogEntry>): List<LogEntry> {
        return events.filter {
            (it.hasRelevantType()) || it.isUserAction()
        }
    }

    private fun copyEvents(inputList: List<LogEntry>): List<LogEntry> {
        val outputList = mutableListOf<LogEntry>()
        inputList.forEach {
            outputList.add(copyEvent(it))
        }
        return outputList
    }

    private fun copyEvent(input: LogEntry): LogEntry {
        val resourceSpecification = ResourceSpecification(input.url, input.subType)
        val output = LogEntry(
            rs = resourceSpecification,
            method = input.method,
            request = input.request,
            createdAt = input.createdAt
        )
        output.title = input.title
        output.type = input.type
        output.obj = input.obj
        output.state = input.state
        output.response = input.response
        return output
    }

    private fun LogEntry.isUserAction(): Boolean {
        return this.state == EventState.USER_ACTION
    }

    private fun LogEntry.hasRelevantType(): Boolean {
        return this.type == Represention.HOMEPAGE.type ||
                this.type == Represention.OBJECT_ACTION.type
    }

}