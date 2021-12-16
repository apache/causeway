/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.isis.client.kroviz.ui.dialog

import io.kvision.core.CssSize
import io.kvision.core.FlexDirection
import io.kvision.core.UNIT
import io.kvision.panel.Direction
import io.kvision.panel.SplitPanel
import io.kvision.panel.VPanel
import org.apache.isis.client.kroviz.core.event.EventStore
import org.apache.isis.client.kroviz.core.event.LogEntry
import org.apache.isis.client.kroviz.core.event.LogEntryComparison
import org.apache.isis.client.kroviz.core.event.ResourceSpecification
import org.apache.isis.client.kroviz.ui.core.RoDialog
import org.apache.isis.client.kroviz.ui.core.SessionManager
import org.apache.isis.client.kroviz.ui.panel.EventLogTable
import org.apache.isis.client.kroviz.utils.StringUtils

class EventReplayDialog(
    private val expectedEvents: List<LogEntry>,
    title: String
) : Controller() {

    private val expectedPanel = VPanel(spacing = 3) {
        width = CssSize(20, UNIT.perc)
    }
    private val actualPanel = VPanel(spacing = 3) {
        width = CssSize(80, UNIT.perc)
    }

    init {
        dialog = RoDialog(
            caption = title,
            items = mutableListOf(),
            controller = this,
            defaultAction = "Compare",
            widthPerc = 60,
            heightPerc = 70,
            customButtons = mutableListOf()
        )
        val expectedTable = EventLogTable(expectedEvents)
        expectedTable.tabulator.addCssClass("tabulator-in-dialog")
        val actualEvents: MutableList<LogEntry> = SessionManager.getEventStore().log

        val actualTable = EventLogTable(actualEvents)
        actualTable.tabulator.addCssClass("tabulator-in-dialog")
        expectedPanel.add(expectedTable)
        actualPanel.add(actualTable)

        val splitPanel = SplitPanel(direction = Direction.VERTICAL)
        splitPanel.addCssClass("dialog-content")
        splitPanel.flexDirection = FlexDirection.ROW
        splitPanel.add(expectedPanel)
        splitPanel.add(actualPanel)
        dialog.formPanel!!.add(splitPanel)
    }

    override fun execute(action: String?) {
        val comparisonMap = mutableMapOf<String, LogEntryComparison>()
        // first pass: iterate over expected
        val actualStore = SessionManager.getEventStore()
        expectedEvents.forEach {
            val shortTitle = StringUtils.shortTitle(it.title)
            val actualEvent: LogEntry? = actualStore.findBy(shortTitle)
            val lec = LogEntryComparison(shortTitle, it, actualEvent)
            comparisonMap.put(shortTitle, lec)
        }

        // second pass: iterate over actual
        val actualEvents = actualStore.log
        val expectedStore = EventStore()
        expectedStore.log.addAll(expectedEvents)
        actualEvents.forEach {
            val shortTitle = StringUtils.shortTitle(it.title)
            if (!comparisonMap.contains(shortTitle)) {
                val rs = ResourceSpecification(it.url, it.subType)
                val expectedEvent = expectedStore.findBy(rs)
                val lec = LogEntryComparison(shortTitle, expectedEvent, it)
                comparisonMap.put(shortTitle, lec)
            }
        }
        val comparisonList = mutableListOf<LogEntryComparison>()
        comparisonList.addAll(comparisonMap.values)
        EventCompareDialog(comparisonList).open()
    }

}
