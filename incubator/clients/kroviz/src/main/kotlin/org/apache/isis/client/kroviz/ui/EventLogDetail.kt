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

import org.apache.isis.client.kroviz.core.event.LogEntry
import org.apache.isis.client.kroviz.to.ValueType
import org.apache.isis.client.kroviz.ui.kv.Constants
import org.apache.isis.client.kroviz.ui.kv.RoDialog
import org.apache.isis.client.kroviz.utils.Utils

class EventLogDetail(val logEntry: LogEntry) : Command() {

    fun open() {
        val formItems = mutableListOf<FormItem>()

        formItems.add(FormItem("Url", ValueType.TEXT, logEntry.url))

        var jsonStr = logEntry.response
        if (jsonStr.isNotEmpty() && logEntry.subType == Constants.subTypeJson) {
            jsonStr = Utils.format(jsonStr)
        }
        formItems.add(FormItem("Response", ValueType.TEXT_AREA, jsonStr, 10))

        var aggtStr = ""
        logEntry.aggregators.forEach { it ->
            aggtStr += it.toString()
        }
        formItems.add(FormItem("Aggregators", ValueType.TEXT_AREA, aggtStr, 5))

        RoDialog(
                caption = "Details :" + logEntry.title,
                items = formItems,
                command = this,
                defaultAction = "Debug",
                widthPerc = 60).open()
    }

    override fun execute() {
        console.log(logEntry)
    }

}
