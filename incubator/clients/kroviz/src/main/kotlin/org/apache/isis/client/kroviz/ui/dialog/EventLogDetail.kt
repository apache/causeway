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
import org.apache.isis.client.kroviz.to.ValueType
import org.apache.isis.client.kroviz.ui.core.FormItem
import org.apache.isis.client.kroviz.ui.diagram.PumlBuilder
import org.apache.isis.client.kroviz.ui.core.Constants
import org.apache.isis.client.kroviz.ui.core.RoDialog
import org.apache.isis.client.kroviz.utils.Utils
import org.apache.isis.client.kroviz.utils.XmlHelper

class EventLogDetail(val logEntry: LogEntry) : Command() {

    fun open() {
        val formItems = mutableListOf<FormItem>()

        formItems.add(FormItem("Url", ValueType.TEXT, logEntry.url))

        var responseStr = logEntry.response
        if (logEntry.subType == Constants.subTypeJson) {
            responseStr = Utils.format(responseStr)
        } else {
            responseStr = XmlHelper.formatXml(responseStr)
        }
        formItems.add(FormItem("Response", ValueType.TEXT_AREA, responseStr, 10))

        var aggtStr = ""
        logEntry.aggregators.forEach { it ->
            aggtStr += it.toString()
        }
        formItems.add(FormItem("Aggregators", ValueType.TEXT_AREA, aggtStr, 5))

        RoDialog(
                caption = "Details :" + logEntry.title,
                items = formItems,
                command = this,
                defaultAction = "Diagram",
                widthPerc = 60).open()
    }


    override fun execute() {
        val str = logEntry.response
        val json = when {
            str.startsWith("<") -> {
                XmlHelper.xml2json(str)
            }
            str.startsWith("{") -> str
            else -> "{}"
        }
        val pumlCode = PumlBuilder().asJsonDiagram(json)
        DiagramDialog("Response Diagram", pumlCode).open()
    }

    fun executeConsole() {
        console.log(logEntry)
    }

}
