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
package org.apache.isis.client.kroviz.ui.kv

import org.apache.isis.client.kroviz.core.event.EventStore
import org.apache.isis.client.kroviz.core.event.LogEntry
import pl.treksoft.kvision.chart.DataSets
import pl.treksoft.kvision.core.Color
import kotlin.math.max
import kotlin.js.Date

class EventChartModel(log: List<LogEntry>) : ChartModel {

    override var bgColorList = mutableListOf<Color>()
    override var bgColorList2 = mutableListOf<Color>()
    override var datasetList = mutableListOf<DataSets>()
    override var labelList = mutableListOf<String>()
    val dataList = mutableListOf<Int>()
    override var ds1 = DataSets()
    override var ds2 = DataSets()

    private val violett = Color.hex(0x8064A2)
    private val red = Color.hex(0xC0504D)
    private val yellow = Color.hex(0xF79646)
    private val green = Color.hex(0x9BBB59)
    private val blue = Color.hex(0x4F81BD)

    private var startTime = Date()

    init {
        val maxResponseLength = EventStore.maxResponseLength()
        var i = 0
        log.forEach { le ->
            i += 1
            if (i == 1) startTime = le.createdAt
            val q = le.responseLength.div(maxResponseLength)
            when {
                (q >= 0) && (q <= 0.2) -> bgColorList.add(blue)
                (q > 0.2) && (q <= 0.4) -> bgColorList.add(green)
                (q > 0.4) && (q <= 0.6) -> bgColorList.add(yellow)
                (q > 0.6) && (q <= 0.8) -> bgColorList.add(red)
                else -> bgColorList.add(violett)
            }
            labelList.add(le.toLabel(i))
            dataList.add(le.duration)
        }
        ds1 = DataSets(
                data = dataList,
                backgroundColor = bgColorList,
                label = "duration"
        )

        datasetList.add(ds1)
    }

    fun LogEntry.toLabel(index: Int): String {
        return index.toString() + "\n" +
                (this.createdAt.getTime() - startTime.getTime()).toString() + "\n" +
                this.title + "\n" +
                "start: " + this.createdAt.toISOString() + "\n" +
                "rsp.len: " + this.responseLength
    }

    fun EventStore.maxResponseLength(): Int {
        var maxRL = 0
        log.forEach { le ->
            maxRL = max(maxRL, le.responseLength)
        }
        return maxRL
    }

}
