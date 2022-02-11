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

//mport io.data2viz.viz.bindRendererOn
import io.kvision.html.Canvas
import org.apache.isis.client.kroviz.core.event.LogEntry
import org.apache.isis.client.kroviz.ui.core.RoDialog
import org.apache.isis.client.kroviz.ui.diagram.Data2VizDiagram
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
     /*       val viz = Data2VizDiagram().bubbleChartExample()
            val htmlCanvas = canvas.getElement().unsafeCast<HTMLCanvasElement>()
            viz.bindRendererOn(htmlCanvas) */
        }
        dialog.add(canvas)
    }

}

