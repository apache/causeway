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

import io.kvision.chart.*
import io.kvision.chart.js.LegendItem
import io.kvision.core.Color
import io.kvision.core.CssSize
import io.kvision.core.UNIT
import io.kvision.panel.SimplePanel
import io.kvision.utils.obj
import org.apache.causeway.client.kroviz.core.event.LogEntry
import org.apache.causeway.client.kroviz.ui.core.SessionManager
import org.apache.causeway.client.kroviz.ui.dialog.EventLogDetail
import org.apache.causeway.client.kroviz.utils.StringUtils
import kotlin.math.pow

@OptIn(ExperimentalJsExport::class)
@JsExport
fun openLogEntry(i: Int) {
    val logEntry = SessionManager.getEventStore().log[i]
    EventLogDetail(logEntry).open()
}

class EventBubbleChart : SimplePanel() {
    private val model = SessionManager.getEventStore()
    private val logStart = model.getLogStartMilliSeconds()
    private var chart: Chart

    init {
        width = CssSize(90, UNIT.vw)
        chart = chart(
            configuration = buildConfiguration()
        )
    }

    private fun buildConfiguration(): Configuration {
        fun buildToolTipList(): List<String> {
            val labelList = mutableListOf<String>()
            model.log.forEachIndexed { i, it ->
                val l = it.buildToolTip(i)
                labelList.add(l)
            }
            return labelList
        }

        val dataSetsList = listOf(buildDataSets())
        return Configuration(
            type = ChartType.BUBBLE,
            dataSets = dataSetsList,
            labels = buildToolTipList(),
            options = buildChartOptions(),
        )
    }

    private fun buildChartOptions(): ChartOptions {
        fun buildLegend(): LegendOptions {
            fun buildLegendLabelList(): Array<LegendItem> {
                val legendLabelList = mutableListOf<LegendItem>()
                label2color.forEach {
                    val li = obj {
                        text = it.key
                        fillStyle = it.value
                    }
                    legendLabelList.add(li as LegendItem)
                }
                val error = obj {
                    text = "error"
                    fillStyle = TRANSPARENT
                    strokeStyle = ERROR_COLOR
                }
                legendLabelList.add(error as LegendItem)
                val running = obj {
                    text = "running"
                    fillStyle = TRANSPARENT
                    strokeStyle = RUNNING_COLOR
                }
                legendLabelList.add(running as LegendItem)
                val size = obj {
                    text = "bubble size ^= response bytes"
                    fillStyle = TRANSPARENT
                    strokeStyle = TRANSPARENT
                }
                legendLabelList.add(size as LegendItem)
                return legendLabelList.toTypedArray()
            }

            return LegendOptions(
                display = true,
                position = Position.RIGHT,
                labels = LegendLabelOptions(generateLabels = {
                    buildLegendLabelList()
                }),
                title = LegendTitleOptions(text = "Parallel Requests", display = true),
            )
        }

        return ChartOptions(
            maintainAspectRatio = true,
            plugins = PluginsOptions(
                title = TitleOptions(
                    text = listOf("Request Duration over Time by Request Parallelism and Response Bytes"),
                    display = true
                ),
                tooltip = TooltipOptions(
                    callbacks = TooltipCallback(
                        footer = tooltipCallbackFooterJsFunction()
                    )
                ),
                legend = buildLegend(),
            ),
            onClick = onClickJsFunction(),
            showLine = true,
            scales = mapOf(
                "x" to ChartScales(
                    title = ScaleTitleOptions(
                        text = "Time since Connect(ms)", display = true
                    )
                ),
                "y" to ChartScales(
                    title = ScaleTitleOptions(text = "duration in ms (log)", display = true),
                    type = ScalesType.LOGARITHMIC
                )
            )
        )
    }

    private fun buildDataSets(): DataSets {
        fun buildBgColorList(): List<Color> {
            val bgColorList = mutableListOf<Color>()
            model.log.forEach {
                val c = it.determineBubbleColor()
                bgColorList.add(c)
            }
            return bgColorList
        }

        fun buildBorderColorList(): List<Color> {
            val borderColorList = mutableListOf<Color>()
            model.log.forEach {
                when {
                    it.isError() -> borderColorList.add(ERROR_COLOR)
                    it.isRunning() -> borderColorList.add(RUNNING_COLOR)
                    else -> borderColorList.add(TRANSPARENT)
                }
            }
            return borderColorList
        }

        /**
         * The term DataSets is severely miss leading:
         * 1. a plural form is used (where actually a singular would be more appropriate) -> "a DataSets"
         * 2. datasets are used inside datasets, data inside data
         */
        fun buildDataSetsList(): List<DataSets> {
            val dataSetsList = mutableListOf<DataSets>()
            model.log.forEach {
                val d = it.buildData()
                dataSetsList.add(d)
            }
            return dataSetsList
        }

        return DataSets(
            backgroundColor = buildBgColorList(),
            borderColor = buildBorderColorList(),
            data = buildDataSetsList(),
        )
    }

    private fun LogEntry.buildToolTip(index: Int): String {
        val size = StringUtils.format(this.responseLength)
        val ms = StringUtils.format(this.duration)
        val title = StringUtils.shortTitle(this.title)
        return title +
                "\nseq.no.: $index" +
                "\nparallel runs: ${this.runningAtStart}" +
                "\nrsp.len.: $size" +
                "\nduration: $ms" +
                "\ntype: ${this.type}"
    }

    private fun LogEntry.calculateBubbleSize(): Int {
        val i = responseLength
        return i.toDouble().pow(1 / 4.toDouble()).toInt()
    }

    private fun LogEntry.determineBubbleColor(): Color {
        val i = runningAtStart
        return when {
            (i >= 0) && (i <= 4) -> LIGHT_BLUE
            (i > 4) && (i <= 8) -> DARK_BLUE
            (i > 8) && (i <= 16) -> GREEN
            (i > 16) && (i <= 32) -> YELLOW
            (i > 32) && (i <= 64) -> RED
            (i > 64) && (i <= 128) -> RED_VIOLET
            else -> VIOLET
        }
    }

    private fun LogEntry.buildData(): dynamic {
        var time = createdAt.getTime()
        if (updatedAt != null) {
            time = updatedAt!!.getTime()
        }
        val relativeTimeMs = time - logStart
        val bubbleSize = calculateBubbleSize()
        val data = obj {
            x = relativeTimeMs
            y = duration
            r = bubbleSize
        }
        return data
    }

    companion object {
        val TRANSPARENT = Color.rgba(0xFF, 0xFF, 0xFF, 0x00)
        val ERROR_COLOR = Color.rgba(0xFF, 0x00, 0x00, 0xFF)
        val RUNNING_COLOR = Color.rgba(0xFF, 0xFF, 0x00, 0xFF)
        val LIGHT_BLUE = Color.rgba(0x4B, 0xAC, 0xC6, 0x80)
        val DARK_BLUE = Color.rgba(0x4F, 0x81, 0xBD, 0x80)
        val GREEN = Color.rgba(0x9B, 0xBB, 0x59, 0x80)
        val YELLOW = Color.rgba(0xF7, 0x96, 0x46, 0x80)
        val RED = Color.rgba(0xC0, 0x50, 0x4D, 0x80)
        val RED_VIOLET = Color.rgba(0xA0, 0x5A, 0x78, 0x80)
        val VIOLET = Color.rgba(0x80, 0x64, 0xA2, 0x80)

        val label2color = mapOf(
            "0 .. 4" to LIGHT_BLUE,
            "5 .. 8" to DARK_BLUE,
            "9 .. 16" to GREEN,
            "17 .. 32" to YELLOW,
            "33 .. 64" to RED,
            "65 .. 128" to RED_VIOLET,
            ">= 129" to VIOLET
        )

        fun onClickJsFunction(): dynamic {
            return js(
                """function(e) {
                        var element = e.chart.getElementsAtEventForMode(e, 'nearest', {intersect: true}, true);
                        if (element.length > 0) {
                            var i = element[0].index;
                            kroviz.org.apache.causeway.client.kroviz.ui.panel.openLogEntry(i);
                            }
                        }"""
            )
        }

        fun tooltipCallbackFooterJsFunction(): dynamic {
            return js(
                """function(context) {
                            var ctx = context[0];
                            var chart = ctx.chart;
                            var ccc = chart.config._config;
                            var data = ccc.data;
                            var i = ctx.dataIndex;
                            return data.labels[i];
                }"""
            )
        }
    }

}
