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
import org.apache.isis.client.kroviz.to.ResultValue
import org.apache.isis.client.kroviz.to.ValueType
import org.apache.isis.client.kroviz.ui.core.FormItem
import org.apache.isis.client.kroviz.ui.core.RoDialog

class FileDialog(val logEntry: LogEntry) : Controller() {

    override fun open() {
        val rv = logEntry.getTransferObject() as ResultValue
        val rvr = rv.result!!
        val value = rvr.value!!.content as String
        val list = value.split(":")
        val formItems = mutableListOf<FormItem>()
        formItems.add(FormItem("URL", ValueType.TEXT, logEntry.url))
        formItems.add(FormItem("Blob", ValueType.TEXT_AREA, list[1], 15))
        val label = list[0] + "/" + list[1]
        RoDialog(
                caption = label,
                items = formItems,
                controller = this).open()
    }

}
