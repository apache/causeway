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

import io.kvision.chart.*
import io.kvision.chart.js.LegendItem
import io.kvision.core.Color
import io.kvision.core.CssSize
import io.kvision.core.UNIT
import io.kvision.panel.SimplePanel
import io.kvision.utils.obj
import org.apache.isis.client.kroviz.core.event.LogEntry
import org.apache.isis.client.kroviz.ui.dialog.EventLogDetail
import org.apache.isis.client.kroviz.ui.core.SessionManager
import kotlin.math.pow

@OptIn(kotlin.js.ExperimentalJsExport::class)
@JsExport
fun openLogEntry(i: Int) {
    val logEntry = SessionManager.getEventStore().log[i]
    EventLogDetail(logEntry).open()
}

@OptIn(kotlin.js.ExperimentalJsExport::class)
@JsExport
fun foo() = "Hello"

class EventBubbleChart() : SimplePanel() {
    private val model = SessionManager.getEventStore()
    private val logStart = model.getLogStartMilliSeconds()
    private var chart: Chart

    init {
        width = CssSize(90, UNIT.vw)
        chart = buildChart()
    }

    private fun buildChart(): Chart {
        return chart(
            configuration = buildConfiguration(),
        )
    }

    private fun buildConfiguration(): Configuration {
        val dataSetsList = listOf(buildDataSets())
        return Configuration(
            type = ChartType.BUBBLE,
            dataSets = dataSetsList,
            labels = buildLabels(),
            options = buildChartOptions(dataSetsList)
        )
    }

    // https://stackoverflow.com/questions/45249779/chart-js-bubble-chart-changing-dataset-labels
    private fun buildChartOptions(dataSetsList: List<DataSets>): ChartOptions {
        val chartOptions = ChartOptions(
            plugins = PluginsOptions(
                title = TitleOptions(
                    text = listOf<String>("Request Duration over Time by Request Density and Response Size"),
                    display = true
                ),
                tooltip = TooltipOptions(callbacks = toolTipCallback())
            ),
            onClick = js(
                "function(e) {"
                        + "var element = e.chart.getElementsAtEventForMode(e, 'nearest', {intersect: true}, true);"
                        + "if (element.length > 0) {"
                        + "var i = element[0].index;"
                        + "console.log(i);"
                        + "kroviz.org.apache.isis.client.kroviz.ui.panel.openLogEntry(i);"
                        + "alert(kroviz.org.apache.isis.client.kroviz.ui.panel.hello());"
                        + "}"
                        + "}"
            ),
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
        return chartOptions
    }

    // https://www.youtube.com/watch?v=UxJ5d-HGhJA
    // https://en.wikipedia.org/wiki/Clarke%27s_three_laws -> #2
    // I would have appreciated a real API.
    private fun toolTipCallback(): TooltipCallback {
        return TooltipCallback(
            footer = js(
                "function(context) {"
                        + "var ctx = context[0];"
                        + "var chart = ctx.chart;"
                        + "var ccc = chart.config._config;"
                        + "var data = ccc.data;"
                        + "var i = ctx.dataIndex;"
                        + "return data.labels[i];"
                        + "}"
            )
        )
    }

    private fun LogEntry.toLabel(index: Int): String {
        return this.title +
                "\nseq.no.: $index" +
                "\nparallel runs: ${this.runningAtStart}" +
                "\nrsp.len.: ${this.responseLength}" +
                "\ntype: ${this.type}"
    }

    private fun buildDataSets(): DataSets {
        return DataSets(
            backgroundColor = buildBgColorList(),
            data = buildData()
        )
    }

    /**
     * The term DataSets is severely miss leading:
     * 1. a plural form is used (where actually a singular would be more appropriate) -> "a DataSets"
     */
    private fun buildData(): List<DataSets> {
        val dataSets = mutableListOf<DataSets>()
        model.log.forEach {
            dataSets.add(it.asData())
        }
        return dataSets
    }

    private fun LogEntry.asData(): dynamic {
        val relativeStartTimeMs = createdAt.getTime() - logStart
        val bubbleSize = calculateBubbleSize()
        val data = obj {
            x = relativeStartTimeMs
            y = duration
            r = bubbleSize
        }
        return data
    }

    private fun buildBgColorList(): List<Color> {
        val bgColorList = mutableListOf<Color>()
        model.log.forEach {
            val c = it.calculateBubbleColor()
            bgColorList.add(c)
        }
        return bgColorList
    }

    private fun LogEntry.calculateBubbleColor(): Color {
        val i = runningAtStart
        return when {
            (i >= 0) && (i <= 4) -> EventBubbleChart.LIGHT_BLUE
            (i > 4) && (i <= 8) -> EventBubbleChart.DARK_BLUE
            (i > 8) && (i <= 16) -> EventBubbleChart.GREEN
            (i > 16) && (i <= 32) -> EventBubbleChart.YELLOW
            (i > 32) && (i <= 64) -> EventBubbleChart.RED
            (i > 64) && (i <= 128) -> EventBubbleChart.RED_VIOLET
            else -> EventBubbleChart.VIOLET
        }
    }

    private fun LogEntry.calculateBubbleSize(): Int {
        val i = responseLength
        return i.toDouble().pow(1 / 3.toDouble()).toInt()
    }

    private fun buildLabels(): List<String> {
        val labelList = mutableListOf<String>()
        model.log.forEachIndexed { i, it ->
            val l = it.toLabel(i)
            labelList.add(l)
        }
        return labelList
    }

    private fun buildLegendLabelOptions(): LegendLabelOptions {
        val legend = LegendLabelOptions()
        legend.generateLabels?.invoke() {

        }
        return legend
    }

    private fun generateLabels(): List<LegendItem> {
        return mutableListOf<LegendItem>()
    }

    companion object {
        val VIOLET = Color.rgba(0x80, 0x64, 0xA2, 0x80)
        val RED_VIOLET = Color.rgba(0xA0, 0x5A, 0x78, 0x80)
        val RED = Color.rgba(0xC0, 0x50, 0x4D, 0x80)
        val YELLOW = Color.rgba(0xF7, 0x96, 0x46, 0x80)
        val GREEN = Color.rgba(0x9B, 0xBB, 0x59, 0x80)
        val LIGHT_BLUE = Color.rgba(0x4B, 0xAC, 0xC6, 0x80)
        val DARK_BLUE = Color.rgba(0x4F, 0x81, 0xBD, 0x80)
    }

}
