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
import io.kvision.form.text.TextArea
import io.kvision.panel.Direction
import io.kvision.panel.SplitPanel
import io.kvision.panel.VPanel
import org.apache.isis.client.kroviz.core.event.LogEntryComparison
import org.apache.isis.client.kroviz.ui.core.RoDialog

class ResponseComparisonDialog(obj: LogEntryComparison) : Command() {
    private val title = "Response Diff"

    private val expectedPanel = VPanel(spacing = 3) {
        width = CssSize(50, UNIT.perc)
    }
    private val actualPanel = VPanel(spacing = 3) {
        width = CssSize(50, UNIT.perc)
    }

    init {
        dialog = RoDialog(
            caption = title,
            items = mutableListOf(),
            command = this,
            widthPerc = 80,
            heightPerc = 70,
            customButtons = mutableListOf()
        )
        val expectedText = TextArea(label = "Expected", value = obj.expectedResponse, rows = 30)
        expectedPanel.add(expectedText)
        val actualText = TextArea(label = "Actual", value = obj.actualResponse, rows = 30)
        actualPanel.add(actualText)

        val splitPanel = SplitPanel(direction = Direction.VERTICAL)
        splitPanel.addCssClass("dialog-content")
        splitPanel.flexDirection = FlexDirection.ROW
        splitPanel.add(expectedPanel)
        splitPanel.add(actualPanel)
        dialog.formPanel!!.add(splitPanel)
        dialog.open()
    }

}
