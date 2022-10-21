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

package org.apache.causeway.client.kroviz.ui.menu

import io.kvision.utils.obj
import org.apache.causeway.client.kroviz.core.event.EventLogStatistics
import org.apache.causeway.client.kroviz.core.event.ResourceProxy
import org.apache.causeway.client.kroviz.to.TObject
import org.apache.causeway.client.kroviz.ui.core.ViewManager
import org.apache.causeway.client.kroviz.ui.dialog.EventExportDialog
import org.apache.causeway.client.kroviz.ui.panel.EventBubbleChart
import org.apache.causeway.client.kroviz.ui.panel.EventLogTable
import org.apache.causeway.client.kroviz.utils.IconManager
import org.apache.causeway.client.kroviz.utils.StringUtils

object DynamicMenuBuilder {

    fun buildObjectMenu(tObject: TObject): dynamic {
        val menu = mutableListOf<dynamic>()
        val actions = tObject.getActions()
        actions.forEach {
            val title = StringUtils.deCamel(it.id)
            val icon = IconManager.find(title)
            val invokeLink = it.getInvokeLink()!!
            val command = { ResourceProxy().fetch(invokeLink) }
            val me = buildMenuEntry(icon, title, command)
            menu.add(me)
        }
        return menu.toTypedArray().asDynamic()
    }

    fun buildTableMenu(table: EventLogTable): dynamic {
        val menu = mutableListOf<dynamic>()

        val export = buildMenuEntry("Export", "Export Events ...", {
            EventExportDialog().open()
        })
        menu.add(export)

        val download = buildMenuEntry("Tabulator Download", "Tabulator Download", {
            this.downLoadCsv(table)
        })
        menu.add(download)

        val bubbleTitle = "Event Bubble Chart"
        val bubble = buildMenuEntry(bubbleTitle, bubbleTitle, {
            ViewManager.add(bubbleTitle, EventBubbleChart())
        })
        menu.add(bubble)

        val statsTitle = "Event Statistics"
        val stats = buildMenuEntry(statsTitle, statsTitle, {
            logStatistics()
        })
        menu.add(stats)

        return menu.toTypedArray().asDynamic()
    }

    private fun buildMenuEntry(icon: String, title: String, act: dynamic): dynamic {
        val iconName = IconManager.find(icon)
        val l = "<i class='$iconName'></i> $title"
        return obj {
            label = l
            action = act
        }
    }

    private fun downLoadCsv(table: EventLogTable) {
        table.tabulator.downloadCSV("data.csv")
    }

    private fun logStatistics() {
        val stats = EventLogStatistics()
        console.log(stats)
    }

}
