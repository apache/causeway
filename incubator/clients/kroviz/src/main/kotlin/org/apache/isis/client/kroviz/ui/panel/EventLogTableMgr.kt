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

package org.apache.isis.client.kroviz.ui.panel

import org.apache.isis.client.kroviz.core.event.EventStore
import org.apache.isis.client.kroviz.ui.dialog.DiagramDialog
import org.apache.isis.client.kroviz.ui.dialog.EventExportDialog
import org.apache.isis.client.kroviz.ui.diagram.UmlDiagram
import org.apache.isis.client.kroviz.ui.chart.ChartFactory
import org.apache.isis.client.kroviz.ui.core.UiManager
import org.apache.isis.client.kroviz.utils.IconManager

class EventLogTableMgr {
    private class UIAction(val label: String, val action: dynamic) {}

    fun buildTableMenu(table: EventLogTable): dynamic {
        val menu = mutableListOf<UIAction>()

        val a1 = UIAction(buildLabel("Hierarchy", "Event Diagram"), {
            this.eventDiagram()
        })
        menu.add(a1)

        val a2 = UIAction(buildLabel("Export", "Export Events ..."), {
            EventExportDialog().open()
        })
        menu.add(a2)

        val a3 = UIAction(buildLabel("Tabulator Download", "Tabulator Download"), {
            this.downLoadCsv(table)
        })
        menu.add(a3)

        val title = "Chart"
        val a4 = UIAction(buildLabel(title, title), {
            UiManager.add(title, ChartFactory().build(EventStore.log))
        })
        menu.add(a4)


        return menu.toTypedArray().asDynamic()
    }

    private fun eventDiagram() {
        val code = UmlDiagram.buildSequence(EventStore.log)!!
        DiagramDialog("Event Diagram", code).open()
    }

    private fun downLoadCsv(table: EventLogTable) {
        table.tabulator.downloadCSV("data.csv")
    }

    private fun buildLabel(icon: String, title: String): String {
        val iconName = IconManager.find(icon)
        return "<i class='$iconName'></i> $title"
    }

}
