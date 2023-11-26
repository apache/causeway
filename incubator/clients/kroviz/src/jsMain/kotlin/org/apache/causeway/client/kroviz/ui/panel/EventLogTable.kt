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

import io.kvision.core.AlignItems
import io.kvision.core.Border
import io.kvision.core.FlexWrap
import io.kvision.panel.VPanel
import io.kvision.panel.hPanel
import io.kvision.tabulator.*
import io.kvision.tabulator.js.Tabulator.CellComponent
import io.kvision.utils.obj
import io.kvision.utils.px
import org.apache.causeway.client.kroviz.core.event.EventState
import org.apache.causeway.client.kroviz.core.event.LogEntry
import org.apache.causeway.client.kroviz.ui.builder.TableBuilder
import org.apache.causeway.client.kroviz.ui.dialog.EventLogDetail
import org.apache.causeway.client.kroviz.ui.menu.DynamicMenuBuilder

class EventLogTable(val model: List<LogEntry>, filterState: EventState? = null) : VPanel() {
    val tabulator: Tabulator<dynamic>

    private val columns = listOf(
        buildCdForTableMenu(),
        buildCdForTitle(),
        buildCdForType(),
        buildCdForState(),
        buildCdForMethod(),
        buildCdForAggregators(),
        buildCdForRequestLength(),
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
            field = "icon_1",
            width = "50",
            headerMenu = DynamicMenuBuilder().buildTableMenu(this),
            hozAlign = Align.CENTER,
            vertAlign = VAlign.BOTTOM,
            formatter = Formatter.HTML,
            clickMenu = { _: dynamic, cellComponent: CellComponent ->
                val le = getObjectFromCell(cellComponent)
                EventLogDetail(le).open()
            }
            //val tto = TooltipOptions(title = data.title)
            // tabulator tooltip is buggy: often the tooltip doesn't go away and the color is not settable
            //b.enableTooltip(tto)
            //      if (le.obj is TObject) b.setDragDropData(Constants.stdMimeType, le.url)
        )
    }

    private fun buildCdForTitle(): ColumnDefinition<dynamic> {
        return ColumnDefinition(
            download = false,
            title = "Title",
            field = "title_1",
            headerFilter = Editor.INPUT,
            width = "700",
            formatter = Formatter.LINK
        )
    }

    private fun buildCdForState(): ColumnDefinition<dynamic> {
        return ColumnDefinition(
            "State",
            "state_1",
            width = "100",
            headerFilter = Editor.INPUT,
            download = false
        )
    }

    private fun buildCdForMethod(): ColumnDefinition<dynamic> {
        return ColumnDefinition(
            "Method",
            "method_1",
            width = "100",
            headerFilter = Editor.INPUT,
            download = false
        )
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

    private fun buildCdForRequestLength(): ColumnDefinition<dynamic> {
        return buildCdForNumber("req.len", "requestLength_1")
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
        return buildCdForNumber("resp.len", "responseLength_1")
    }

    private fun buildCdForCacheHits(): ColumnDefinition<dynamic> {
        return buildCdForNumber("cacheHits", "cacheHits_1")
    }

    private fun buildCdForDuration(): ColumnDefinition<dynamic> {
        return buildCdForNumber("duration", "duration_1")
    }

    private fun buildCdForNumber(title: String, field: String): ColumnDefinition<dynamic> {
        return ColumnDefinition(
            title = title,
            field = field,
            width = "100",
            hozAlign = Align.RIGHT,
            download = false
        )
    }

    private fun buildCdForCreatedAt(): ColumnDefinition<dynamic> {
        return buildCdForDateTime("Created", "createdAt_1")
    }

    private fun buildCdForUpdatedAt(): ColumnDefinition<dynamic> {
        return buildCdForDateTime("Updated", "updatedAt_1")
    }

    private fun buildCdForDateTime(title: String, field: String): ColumnDefinition<dynamic> {
        return ColumnDefinition(
            download = false,
            title = title,
            field = field,
            sorter = Sorter.DATETIME,
            formatter = Formatter.DATETIME,
            formatterParams = obj
            { outputFormat = "HH:mm:ss.SSS" },
            width = "100"
        )
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

    private fun getObjectFromCell(cell: CellComponent): LogEntry {
        val row = cell.getRow()
        return row.getData() as LogEntry
    }

}
