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
package org.apache.causeway.client.kroviz.ui.panel

import io.kvision.core.*
import io.kvision.html.Button
import io.kvision.html.ButtonStyle
import io.kvision.panel.VPanel
import io.kvision.panel.hPanel
import io.kvision.tabulator.*
import io.kvision.utils.obj
import io.kvision.utils.px
import org.apache.causeway.client.kroviz.core.event.EventState
import org.apache.causeway.client.kroviz.core.event.LogEntry
import org.apache.causeway.client.kroviz.to.TObject
import org.apache.causeway.client.kroviz.ui.builder.TableBuilder
import org.apache.causeway.client.kroviz.ui.core.Constants
import org.apache.causeway.client.kroviz.ui.dialog.EventLogDetail
import org.apache.causeway.client.kroviz.ui.menu.DynamicMenuBuilder
import org.apache.causeway.client.kroviz.utils.StringUtils

class EventLogTable(val model: List<LogEntry>, filterState: EventState? = null) : VPanel() {
    val tabulator: Tabulator<dynamic>

    private val columns = listOf(
//        buildCdForTableMenu(),
//        buildCdForTitle(),
        buildCdForType(),
//        buildCdForState(),
        buildCdForMethod(),
        buildCdForAggregators(),
        buildCdForRequestLenght(),
        buildCdForResponse(),
        buildCdForResponseLength(),
        buildCdForCacheHits(),
        buildCdForDuration(),
        buildCdForCreatedAt(),
        buildCdForUpdatedAt()
    )

    private fun buildCdForTableMenu(): ColumnDefinition<dynamic> {
        return ColumnDefinition(
            download = false,
            title = "",
            field = "state_1",
            width = "50",
            headerMenu = DynamicMenuBuilder().buildTableMenu(this),
            hozAlign = Align.CENTER,
            vertAlign = VAlign.MIDDLE,
            formatterComponentFunction = { _, _, data -> buildActionButton(data) }
        )
    }

    private fun buildCdForTitle(): ColumnDefinition<dynamic> {
        return ColumnDefinition(
            download = false,
            title = "Title",
            field = "title_1",
            headerFilter = Editor.INPUT,
            width = "700",
            formatterComponentFunction = { _, _, data -> buildObjectButton(data) })
    }

    private fun buildCdForState(): ColumnDefinition<dynamic> {
        return ColumnDefinition("State", "state_1", width = "100", headerFilter = Editor.INPUT, download = false)
    }

    private fun buildCdForMethod(): ColumnDefinition<dynamic> {
        return ColumnDefinition("Method", "method_1", width = "100", headerFilter = Editor.INPUT, download = false)
    }

    private fun buildCdForAggregators(): ColumnDefinition<dynamic> {
        return ColumnDefinition(
            download = false,
            title = "# of Agg.",
            field = "nOfAggregators_1",
            headerFilter = Editor.INPUT,
            width = "20"
        )
    }

    private fun buildCdForType(): ColumnDefinition<dynamic> {
        return ColumnDefinition(
            download = false,
            title = "Type",
            field = "type_1",
            headerFilter = Editor.INPUT,
            width = "200"
        )
    }

    private fun buildCdForRequestLenght(): ColumnDefinition<dynamic> {
        return ColumnDefinition(
            "req.len",
            field = "requestLength_1",
            width = "100",
            hozAlign = Align.RIGHT,
            download = false
        )
    }

    private fun buildCdForResponse(): ColumnDefinition<dynamic> {
        return ColumnDefinition(
            download = false,
            title = "response",
            field = "response_1",
            headerFilter = Editor.INPUT,
            width = "200",
        )
    }

    private fun buildCdForResponseLength(): ColumnDefinition<dynamic> {
        return ColumnDefinition(
            "resp.len",
            field = "responseLength_1",
            width = "100",
            hozAlign = Align.RIGHT,
            download = false
        )
    }

    private fun buildCdForCacheHits(): ColumnDefinition<dynamic> {
        return ColumnDefinition(
            "cacheHits",
            field = "cacheHits_1",
            width = "100",
            hozAlign = Align.RIGHT,
            download = false
        )
    }

    private fun buildCdForDuration(): ColumnDefinition<dynamic> {
        return ColumnDefinition(
            "duration",
            field = "duration_1",
            width = "100",
            hozAlign = Align.RIGHT,
            download = false
        )
    }

    private fun buildCdForCreatedAt(): ColumnDefinition<dynamic> {
        return ColumnDefinition(
            download = false,
            title = "Created",
            field = "createdAt_1",
            sorter = Sorter.DATETIME,
            formatter = Formatter.DATETIME,
            formatterParams = obj
            { outputFormat = "HH:mm:ss.SSS" },
            width = "100"
        )
    }

    private fun buildCdForUpdatedAt(): ColumnDefinition<dynamic> {
        return ColumnDefinition(
            download = false,
            title = "Updated",
            field = "updatedAt_1",
            sorter = Sorter.DATETIME,
            formatter = Formatter.DATETIME,
            formatterParams = obj
            { outputFormat = "HH:mm:ss.SSS" },
            width = "100"
        )
    }

    private fun buildObjectButton(data: LogEntry): Button {
        val b = Button(
            text = StringUtils.shorten(data.title),
            icon = data.state.iconName,
            style = ButtonStyle.LINK
        )
        b.onClick {
            kotlinx.browser.window.open(data.title) //IMPROVE should be URL
        }
        //val tto = TooltipOptions(title = data.title)
        // tabulator tooltip is buggy: often the tooltip doesn't go away and the color is not settable
        //b.enableTooltip(tto)
        if (data.obj is TObject) b.setDragDropData(Constants.stdMimeType, data.url)
        return b
    }

    private fun buildActionButton(data: LogEntry): Button {
        val b = Button(
            text = "",
            icon = "fa fa-info-circle",
            style = data.state.style
        )
        b.onClick { EventLogDetail(data).open() }
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

        val data = mutableListOf<dynamic>()
        model.forEach {
            data.add(it.asDynamic())
        }
        console.log("[ELT_init]")
        console.log(data)

        tabulator = TableBuilder().createTabulator(data, columns)
        tabulator.setEventListener<Tabulator<dynamic>> {
            mouseover = {
                val jst = tabulator.jsTabulator
                val value = filterState?.name
                if (jst != null && value != null) {
                    jst.setHeaderFilterValue("state", value)
                }
            }
        }
        add(tabulator)
    }

}
