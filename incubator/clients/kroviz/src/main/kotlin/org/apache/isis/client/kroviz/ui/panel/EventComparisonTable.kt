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
package org.apache.isis.client.kroviz.ui.panel

import io.kvision.core.*
import io.kvision.html.Button
import io.kvision.html.ButtonStyle
import io.kvision.panel.VPanel
import io.kvision.panel.hPanel
import io.kvision.tabulator.*
import io.kvision.utils.px
import org.apache.isis.client.kroviz.core.event.LogEntry
import org.apache.isis.client.kroviz.core.event.LogEntryComparison
import org.apache.isis.client.kroviz.to.TObject
import org.apache.isis.client.kroviz.ui.core.Constants
import org.apache.isis.client.kroviz.ui.dialog.EventLogDetail
import org.apache.isis.client.kroviz.utils.StringUtils

class EventComparisonTable(val model: List<LogEntryComparison>) : VPanel() {
    val tabulator: Tabulator<LogEntryComparison>

    private val columns = listOf(
        ColumnDefinition<LogEntryComparison>(
            download = false,
            title = "",
            field = "changeType",
            width = "50",
   //         headerMenu = DynamicMenuBuilder().buildTableMenu(this),
            hozAlign = Align.CENTER,
            vertAlign = VAlign.MIDDLE,
            formatterComponentFunction = { _, _, data -> buildActionButton(data) }
        ),
        ColumnDefinition(
            download = false,
            title = "Title",
            field = "title",
            headerFilter = Editor.INPUT,
            width = "200",
            formatterComponentFunction = { _, _, data -> buildObjectButton(data) }
        ),
        ColumnDefinition(
            download = false,
            title = "expectedResponse",
            field = "expectedResponse",
            headerFilter = Editor.INPUT,
            width = "400",
        ),
        ColumnDefinition(
            download = false,
            title = "actualResponse",
            field = "actualResponse",
            headerFilter = Editor.INPUT,
            width = "400",
        )
    )

    private fun buildObjectButton(data: LogEntryComparison): Button {
        val b = Button(
            text = StringUtils.shorten(data.title),
            icon = data.changeType.iconName,
            style = ButtonStyle.LINK
        )
        b.onClick {
            kotlinx.browser.window.open(data.title) //IMPROVE should be URL
        }
        //val tto = TooltipOptions(title = data.title)
        // tabulator tooltip is buggy: often the tooltip doesn't go away and the color is not settable
        //b.enableTooltip(tto)
    //    if (data.obj is TObject) b.setDragDropData(Constants.stdMimeType, data.url)
        return b
    }

    private fun buildActionButton(data: LogEntryComparison): Button {
        val b = Button(
            text = "",
            icon = "fa fa-info-circle",
            style = data.changeType.style
        )
//        b.onClick { EventLogDetail(data).open() }
        b.margin = CssSize(-10, UNIT.px)
        b.addCssClass("btn-sm")
        return b
    }

    init {
        hPanel(
            FlexWrap.NOWRAP,
            alignItems = AlignItems.CENTER,
            spacing = 20
        ) {
            border = Border(width = 1.px)
        }

        val options = TabulatorOptions(
            movableColumns = true,
            height = Constants.calcHeight,
            layout = Layout.FITCOLUMNS,
            columns = columns,
            persistenceMode = false
        )

        tabulator = tabulator(model, options = options) {
            setEventListener<Tabulator<LogEntryComparison>> {
            }
        }
    }

}
