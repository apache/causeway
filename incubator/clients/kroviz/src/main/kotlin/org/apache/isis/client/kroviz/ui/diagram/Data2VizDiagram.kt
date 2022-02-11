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
package org.apache.isis.client.kroviz.ui.diagram
/*
import io.data2viz.axis.Orient
import io.data2viz.axis.axis
import io.data2viz.color.Colors
import io.data2viz.geom.size
import io.data2viz.random.RandomDistribution
import io.data2viz.scale.Scales
import io.data2viz.viz.TextHAlign
import io.data2viz.viz.TextVAlign
import io.data2viz.viz.Viz
import io.data2viz.viz.viz
import kotlinx.datetime.Instant*/

class Data2VizDiagram {

    // c.f. https://play.data2viz.io/sketches/nLoOzL/edit/
/*    fun bubbleChartExample(): Viz {
        val xSize = 300
        val ySize = 300

        val barHeight = 14.0
        val cPadding = 20.0
        val data = listOf(4, 8, 15, 16, 23, 42)

        val xScale = Scales.Continuous.time {
//            domain = listOf(.0, data.maxOrNull()!!.toDouble())
            range = listOf(.0, xSize - 2 * cPadding)
        }

        val yScale = Scales.Continuous.linear() {
            domain = listOf(.0, data.maxOrNull()!!.toDouble())
            range = listOf(.0, xSize - 2 * cPadding)
        }

        val viz = viz {
            size = size(xSize, ySize)
            data.forEachIndexed { index, datum ->
                group {
                    transform {
                        translate(
                            y = cPadding,
                            x = cPadding + index * (cPadding + barHeight)
                        )
                    }
                    rect {
                        width = yScale(datum.toDouble())
                        height = barHeight
                        fill = Colors.Web.steelblue
                    }
                    circle {
                        radius = datum.toDouble()
                        x = datum.toDouble()
                        fill = Colors.Web.blueviolet.opacify(0.8)
                    }
                    text {
                        textContent = datum.toString()
                        hAlign = TextHAlign.RIGHT
                        vAlign = TextVAlign.HANGING
                        x = yScale(datum.toDouble()) - 2.0
                        y = 1.5
                        textColor = Colors.Web.white
                        fontSize = 10.0
                    }
                    axis(Orient.RIGHT, xScale) {
                        axisStroke = tickStroke
                    }
                }
            }
        }
        return viz
    }

    fun barchartExample(): Viz {
        val vizSize = 500.0
        val barHeight = 14.0
        val cPadding = 2.0
        val data = listOf(4, 8, 15, 16, 23, 42)

        val xScale = Scales.Continuous.linear {
            domain = listOf(.0, data.maxOrNull()!!.toDouble())
            range = listOf(.0, vizSize - 2 * cPadding)
        }

        val viz = io.data2viz.viz.viz {
            size = size(vizSize, vizSize)
            data.forEachIndexed { index, datum ->
                group {
                    transform {
                        translate(
                            x = cPadding,
                            y = cPadding + index * (cPadding + barHeight)
                        )
                    }
                    rect {
                        width = xScale(datum.toDouble())
                        height = barHeight
                        fill = Colors.Web.steelblue
                    }
                    text {
                        textContent = datum.toString()
                        hAlign = TextHAlign.RIGHT
                        vAlign = TextVAlign.HANGING
                        x = xScale(datum.toDouble()) - 2.0
                        y = 1.5
                        textColor = Colors.Web.white
                        fontSize = 10.0
                    }
                }
            }
        }
        return viz
    }
}

val randomGenerator = RandomDistribution(42).normal(100.0, 18.0)

data class Sample(
    val sampleIndex: Int,
    val batchCode: String,
    val timestamp: Instant,
    val temperature: Double,
    val pressure: Double
)

val samples = generateSamples(30)

fun generateSamples(numSamples: Int) = (0 until numSamples).map {
    val batchIndex = 1 + (it % 4)
    val pressure: Double = randomGenerator() * 1000
    val temp: Double = randomGenerator() * batchIndex * pressure / 100000
    val ts = Instant.fromEpochMilliseconds(1611150127144L + (it * 8632L))
    Sample(it, "Batch #$batchIndex", ts, temp, pressure) */
}

