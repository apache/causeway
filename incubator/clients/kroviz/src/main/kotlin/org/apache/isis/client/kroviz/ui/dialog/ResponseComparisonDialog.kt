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

import io.kvision.utils.obj
import org.apache.isis.client.kroviz.core.event.LogEntryComparison
import org.apache.isis.client.kroviz.to.ValueType
import org.apache.isis.client.kroviz.ui.core.FormItem
import org.apache.isis.client.kroviz.ui.core.RoDialog
import org.apache.isis.client.kroviz.utils.js.Diff
import org.apache.isis.client.kroviz.utils.js.Diff2Html

class ResponseComparisonDialog(obj: LogEntryComparison) : Controller() {

    init {
        val html = diff2Html(obj)
        val fi = FormItem("Diff", ValueType.HTML, html, size = 30)
        val formItems = mutableListOf<FormItem>()
        formItems.add(fi)

        val title = "Diff: " + obj.title
        dialog = RoDialog(
            caption = title,
            items = formItems,
            controller = this,
            widthPerc = 80,
            heightPerc = 70,
            customButtons = mutableListOf()
        )
        super.open()
    }

    private fun diff2Html(obj: LogEntryComparison): String {
        val oldText: String = obj.expectedResponse!!
        val newText: String = obj.actualResponse!!
        val diff = Diff.createTwoFilesPatch("file", "file", oldText, newText);
        val options = obj {
            drawFileList = false
            matching = "lines"
            outputFormat = "line-by-line"
        }
        val html = Diff2Html.html(diff, options)
        return html
    }

}
