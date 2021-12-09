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
import io.kvision.core.UNIT
import io.kvision.panel.VPanel
import org.apache.isis.client.kroviz.core.event.LogEntryComparison
import org.apache.isis.client.kroviz.ui.core.RoDialog
import org.apache.isis.client.kroviz.ui.core.UiManager
import org.apache.isis.client.kroviz.ui.panel.EventComparisonTable

class EventCompareDialog(val data: List<LogEntryComparison>) : Controller() {
    private val title = "Event Comparison"
    private var table: EventComparisonTable

    private val panel = VPanel(spacing = 3) {
        width = CssSize(100, UNIT.perc)
    }

    init {
        dialog = RoDialog(
            caption = title,
            items = mutableListOf(),
            controller = this,
            defaultAction = "Pin",
            widthPerc = 60,
            heightPerc = 70,
        )
        //IMPROVE: reuse ColumnFactory and RoTable if possible
        table = EventComparisonTable(data)
        table.tabulator.addCssClass("tabulator-in-dialog")
        panel.add(table)

        dialog.formPanel!!.add(panel)
    }

    override fun execute(action: String?) {
        pin()
    }

    private fun pin() {
        table.tabulator.removeCssClass("tabulator-in-dialog")
        UiManager.add(title, panel)
        dialog.close()
    }

}
