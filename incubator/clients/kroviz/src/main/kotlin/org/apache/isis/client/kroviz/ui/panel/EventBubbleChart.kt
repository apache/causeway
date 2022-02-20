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
import io.kvision.core.Color
import io.kvision.panel.SimplePanel
import io.kvision.utils.obj
import io.kvision.utils.pc
import io.kvision.utils.px
import org.apache.isis.client.kroviz.core.event.LogEntry
import org.apache.isis.client.kroviz.ui.core.SessionManager

class EventBubbleChart() : SimplePanel() {
    private val model = SessionManager.getEventStore()
    private val logStart = model.getLogStartMilliSeconds()

    init {
        this.marginTop = 10.px
        this.width = 100.pc
        buildChart()
    }

    private fun buildChart(): Chart {
        return chart(
            Configuration(
                ChartType.BUBBLE,
                listOf(buildDataSets()),
                options = ChartOptions(
                    plugins = PluginsOptions(legend = LegendOptions(display = true)),
                    showLine = true,
                    scales = mapOf(
                        "x" to ChartScales(title = ScaleTitleOptions(text = "Time since Connect (ms)", display = true)),
                        "y" to ChartScales(title = ScaleTitleOptions(text = "duration (ms)", display = true))
                    )
                )
            )
        )
    }

    private fun buildDataSets(): DataSets {
        val dataSets = DataSets(
//            pointBorderColor = listOf(Color.name(Col.BLACK)),
            backgroundColor = buildBgColorList(),
            //listOf(Color.rgba(155, 187, 89, 196)), //9BBB59
            data = buildData(),
        )
        return dataSets
    }

    private fun buildData(): List<dynamic> {
        return model.log.map {
            it.asData()
       }
    }

    private fun LogEntry.asData() : dynamic {
        val relativeStartTimeMs = createdAt.getTime() - logStart
        val bubbleSize = calculateBubbleSize()
        return obj {
            x = relativeStartTimeMs
            y = duration
            r = bubbleSize
        }
    }

    private fun buildBgColorList(): List<Color> {
        val bgColorList = mutableListOf<Color>()
        model.log.forEach {
            val c = it.calculateBubbleColor()
            bgColorList.add(c)
        }
        console.log("[EBC.buildBgColorList]")
        console.log(bgColorList)
        return bgColorList
    }

    private fun LogEntry.calculateBubbleColor(): Color {
        val violet = Color.hex(0x8064A2)
        val red = Color.hex(0xC0504D)
        val yellow = Color.hex(0xF79646)
        val green = Color.hex(0x9BBB59)
        val blue = Color.hex(0x4F81BD)

        val i = responseLength
        return when {
            (i >= 0) && (i <= 1024) -> blue
            (i > 1024) && (i <= 2048) -> green
            (i > 2048) && (i <= 4096) -> yellow
            (i > 4096) && (i <= 8192) -> red
            else -> violet
        }
    }

    private fun LogEntry.calculateBubbleSize(): Int {
        val i = runningAtStart
        return when {
            (i >= 0) && (i <= 10) -> 2
            (i > 10) && (i <= 20) -> 4
            (i > 20) && (i <= 40) -> 8
            (i > 40) && (i <= 80) -> 16
            (i > 80) && (i <= 160) -> 32
            else -> 64
        }
    }

    private fun buildLabels() {}
    private fun buildLegend() {}

    fun LogEntry.toLabel(index: Int): String {
        val relativeStartTime = ((this.createdAt.getTime() - logStart) / 1000).toString()
        val sec_1 = relativeStartTime.substring(0, relativeStartTime.length - 2)
        return index.toString() + "\n" +
                sec_1 + "\n" +
                this.title + "\n" +
                "start: " + this.createdAt.toISOString() + "\n" +
                "rsp.len: " + this.responseLength
    }

}
