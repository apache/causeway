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

import io.kvision.chart.*
import io.kvision.panel.SimplePanel
import io.kvision.utils.obj
import io.kvision.utils.px
import io.kvision.utils.pc

//IMPROVE https://github.com/datavisyn/chartjs-chart-box-and-violin-plot
class EventChart(model: ChartModel) : SimplePanel() {

    private val font = "'Open Sans Bold', sans-serif"

    private val yAxes = listOf(obj {
        scaleLabel = { ScaleTitleOptions(display = true, labelString = "duration (ms)", fontFamily = font) }
        gridLines = { GridLineOptions(visible = true) }
        ticks = obj {
            fontFamily = font
            fontSize =  11.px
        }
    })

    val tickFormatter:String = "function(tick) {return tick.split('\\n')[1]}"

    private val xAxes = listOf(obj {
        scaleLabel = { ScaleTitleOptions(display = true, labelString = "start offset (sec)", fontFamily = font) }
        gridLines = { GridLineOptions(visible = true) }
        ticks = obj {
            beginAtZero = false
            fontFamily = font
            fontSize = 11.px
            //WHAT A HACK
//            callback = js(tickFormatter)
        }
    })

    private val options = ChartOptions(
            tooltips = TooltipOptions(enabled = true),
            hover = HoverOptions(animationDuration = 0),
            legend = LegendOptions(display = true),
            scales = ChartScales(
                    yAxes = yAxes,
                    xAxes = xAxes),
            showLines = true,
            title = TitleOptions(display = true))

    init {
        this.marginTop = 10.px
        this.width = 100.pc
        chart(
                configuration = Configuration(
                        type = ChartType.BAR,
                        dataSets = model.datasetList,
                        labels = model.labelList,
                        options = options
                ),
                chartHeight = 1000,
                chartWidth = 3000
        )
    }

}
