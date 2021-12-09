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
import org.apache.isis.client.kroviz.ui.core.RoDialog

class UndefinedDialog(val logEntry: LogEntry) : Controller() {

    private val instruction = """1. Create a ResponseClass under test/kotlin/org.ro.urls with
    - url 
    - str (JSON)
2. Create a TestCase under test/kotlin/org.ro.to
3. Implement a TransferObject under main/kotlin/org.ro.to
4. Implement a Handler under main/kotlin/org.ro.handler
5. Amend main/kotlin/org.ro.handler/ResponseHandler by this new Handler"""

    override fun open() {
        val formItems = mutableListOf<FormItem>()
        formItems.add(FormItem("Instructions", ValueType.TEXT_AREA, instruction, size = 7))
        formItems.add(FormItem("URL", ValueType.TEXT, logEntry.url))
        formItems.add(FormItem("JSON", ValueType.TEXT_AREA, logEntry.response, 10))
        val label = "TransferObject has no Handler"
        RoDialog(
                caption = label,
                items = formItems,
                controller = this,
                widthPerc = 80).open()
    }

}
