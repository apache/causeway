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

import pl.treksoft.kvision.chart.*
import pl.treksoft.kvision.core.Color
import pl.treksoft.kvision.panel.SimplePanel
import pl.treksoft.kvision.utils.obj
import pl.treksoft.kvision.utils.px

external fun rgba(r: Int, g: Int, b: Int, a: Int): String

//IMPROVE https://github.com/datavisyn/chartjs-chart-box-and-violin-plot
class EventChart(model:ChartModel) : SimplePanel() {

    private val yAxes = listOf(obj {
        gridLines = {
            display = Display.FLEX
            color = Color.hex(0xffffff)
            zeroLineColor = Color.hex(0xffffff)
//            zeroLineWidth = 1
        }
        ticks = obj {
            fontFamily = "'Open Sans Bold', sans-serif"
            fontSize = 11.px
        }
        stacked = true
    })

    private val xAxes = listOf(obj {
        barThickness = 20
        ticks = obj {
            beginAtZero = true
            fontFamily = "'Open Sans Bold', sans-serif"
            fontSize = 11.px
        }
        gridLines = { GridLineOptions(visible = true) }
        stacked = true
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
        chart(
                configuration = Configuration(
                        type = ChartType.HORIZONTALBAR,
                        dataSets = model.datasetList,
                        labels = model.labelList,
                        options = options
                ),
                chartHeight = 600,
                chartWidth = 1000
        )
    }

}
