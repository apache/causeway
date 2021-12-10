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

import org.apache.isis.client.kroviz.core.event.LogEntry
import org.apache.isis.client.kroviz.core.event.ResourceSpecification
import org.apache.isis.client.kroviz.to.ValueType
import org.apache.isis.client.kroviz.to.bs3.Grid
import org.apache.isis.client.kroviz.ui.core.*
import org.apache.isis.client.kroviz.ui.diagram.JsonDiagram
import org.apache.isis.client.kroviz.ui.diagram.LayoutDiagram
import org.apache.isis.client.kroviz.ui.diagram.LinkTreeDiagram
import org.apache.isis.client.kroviz.utils.js.Flatted
import org.apache.isis.client.kroviz.utils.StringUtils
import org.apache.isis.client.kroviz.utils.XmlHelper

class EventLogDetail(logEntryFromTabulator: LogEntry) : Controller() {
    private var logEntry: LogEntry

    init {
        // For a yet unknown reason, aggregators are not transmitted via tabulator.
        // As a WORKAROUND, we fetch the full-blown LogEntry from the EventStore again.
        val rs = ResourceSpecification(logEntryFromTabulator.title)
        logEntry = SessionManager.getEventStore().findBy(rs) ?: logEntryFromTabulator  // in case of xml, we use the entry passed in
    }

    // callback parameter
    private val LOG: String = "log"
    private val LNK: String = "lnk"
    private val DPM: String = "dpm"

    override fun open() {
        val responseStr = if (logEntry.subType == Constants.subTypeJson) {
            StringUtils.format(logEntry.response)
        } else {
            XmlHelper.format(logEntry.response)
        }

        val formItems = mutableListOf<FormItem>()
        formItems.add(FormItem("Url", ValueType.TEXT, logEntry.title))
        formItems.add(FormItem("Response", ValueType.TEXT_AREA, responseStr, 10))
        formItems.add(FormItem("Aggregators", ValueType.TEXT, content = logEntry.aggregators))

        val customButtons = mutableListOf<FormItem>()
        customButtons.add(FormItem("Link Tree Diagram", ValueType.BUTTON, null, callBack = this, callBackAction = LNK))
        customButtons.add(FormItem("Display Model Diagram", ValueType.BUTTON, null, callBack = this, callBackAction = DPM))
        customButtons.add(FormItem("Console", ValueType.BUTTON, null, callBack = this, callBackAction = LOG))

        dialog = RoDialog(
                caption = "Details :" + logEntry.title,
                items = formItems,
                controller = this,
                defaultAction = "Response Diagram",
                widthPerc = 60,
                customButtons = customButtons)
        super.open()
    }

    override fun execute(action: String?) {
        when {
            action.isNullOrEmpty() -> defaultAction()
            action == LOG -> {
                console.log(logEntry)
            }
            action == LNK -> {
                linkTreeDiagram()
            }
            action == DPM -> {
                displayModelDiagram()
            }
            else -> {
                console.log(logEntry)
                console.log("Action not defined yet: $action")
            }
        }
    }

    private fun linkTreeDiagram() {
        logEntry.aggregators.forEach {
            val code = LinkTreeDiagram.build(it)
            DiagramDialog("Link Tree Diagram", code).open()
        }
        dialog.close()
    }

    private fun displayModelDiagram() {
        logEntry.aggregators.forEach {
            val dpm = it.dpm
            val json = Flatted.stringify(dpm)
            console.log("[ELD.displayModelDiagram]")
            console.log(json)
            val code = JsonDiagram.build(json)
            DiagramDialog("Display Model Diagram", code).open()
        }
        dialog.close()
    }

    private fun defaultAction() {
        val str = logEntry.response
        var label = "Diagram"
        val pumlCode = when {
            str.startsWith("<") -> {
                val grid = logEntry.obj as Grid
                label = "Layout Diagram"
                LayoutDiagram.build(grid)
            }
            str.startsWith("{") -> {
                label = "JSON / XML Diagram"
                JsonDiagram.build(str)
            }
            else -> "{}"
        }
        DiagramDialog(label, pumlCode).open()
    }

}
