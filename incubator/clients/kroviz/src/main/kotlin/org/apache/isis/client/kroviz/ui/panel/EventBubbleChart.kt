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
import io.kvision.core.Col
import io.kvision.core.Color
import io.kvision.panel.SimplePanel
import io.kvision.utils.pc
import io.kvision.utils.px
import org.apache.isis.client.kroviz.ui.chart.EventBubbleChartModel

//IMPROVE https://github.com/datavisyn/chartjs-chart-box-and-violin-plot
class EventBubbleChart(model: EventBubbleChartModel) : SimplePanel() {

    init {
        this.marginTop = 10.px
        this.width = 100.pc
        chart(
            Configuration(
                ChartType.SCATTER,
                listOf(
                    DataSets(
                        pointBorderColor = listOf(Color.name(Col.RED)),
                        backgroundColor = listOf(Color.name(Col.LIGHTGREEN)),
/*                        data = (-60..60).map {
                            obj {
                                x = it.toDouble() / 10
                                y = kotlin.math.sin(it.toDouble() / 10)
                            }
                        } */
                        data = model.dataList
                    )
                ),
                options = ChartOptions(
                    //   plugins = PluginsOptions(legend = LegendOptions(display = false)),
                    //   showLine = true
                )
            )
        )
    }

}
