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
package org.apache.isis.client.kroviz.ui.dialog

import io.data2viz.color.Colors
import io.data2viz.geom.size
import io.data2viz.scale.Scales
import io.data2viz.viz.*
import io.kvision.html.Canvas
import org.apache.isis.client.kroviz.core.event.LogEntry
import org.apache.isis.client.kroviz.ui.core.RoDialog
import org.w3c.dom.HTMLCanvasElement

class ChartDialog(
    private val expectedEvents: List<LogEntry>,
    canvasId: String
) : Controller() {

    init {
        dialog = RoDialog(
            caption = canvasId,
            items = mutableListOf(),
            controller = this,
            defaultAction = "OK",
            widthPerc = 50,
            heightPerc = 50,
            customButtons = mutableListOf()
        )
        val canvas = Canvas(200, 200)
        canvas.addAfterInsertHook {
            val htmlCanvas = canvas.unsafeCast<HTMLCanvasElement>()
            viz().bindRendererOn(htmlCanvas)
        }
        dialog.add(canvas)
    }

    private fun viz(): Viz {
        val vizSize = 500.0
        val barHeight = 14.0
        val cPadding = 2.0
        val data = listOf(4, 8, 15, 16, 23, 42)

        val xScale = Scales.Continuous.linear {
            domain = listOf(.0, data.maxOrNull()!!.toDouble())
            range = listOf(.0, vizSize - 2 * cPadding)
        }

        val viz = viz {
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

