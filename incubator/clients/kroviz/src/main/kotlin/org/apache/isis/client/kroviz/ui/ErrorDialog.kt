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
import org.apache.isis.client.kroviz.to.HttpError
import org.apache.isis.client.kroviz.ui.kv.RoDialog

class ErrorDialog(val logEntry: LogEntry) : Command() {

    fun open() {
        val error = logEntry.getTransferObject() as HttpError
        val formItems = mutableListOf<FormItem>()
        formItems.add(FormItem("URL", "Text", logEntry.url))
        formItems.add(FormItem("Message", "Text", error.message))
        val detail = error.detail
        if (detail != null) {
            formItems.add(FormItem("StackTrace", "TextArea", toString(detail.element), 10))
            formItems.add(FormItem("Caused by", "Text", detail.causedBy))
        }
        val label = "HttpError " + error.httpStatusCode.toString()
        RoDialog(
                caption = label,
                items = formItems,
                command = this,
                widthPerc = 80).open()
    }

    private fun toString(stackTrace: List<String>): String {
        var answer = ""
        for (s in stackTrace) {
            answer += s + "\n"
        }
        return answer
    }

}
