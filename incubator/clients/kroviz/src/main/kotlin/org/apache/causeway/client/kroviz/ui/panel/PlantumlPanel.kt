/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.causeway.client.kroviz.ui.panel

import org.apache.causeway.client.kroviz.utils.UmlUtils
import io.kvision.core.CssSize
import io.kvision.core.UNIT
import io.kvision.form.text.TextArea
import io.kvision.html.Button
import io.kvision.html.ButtonStyle
import io.kvision.panel.Direction
import io.kvision.panel.VPanel
import io.kvision.panel.splitPanel
import io.kvision.panel.vPanel
import io.kvision.utils.px

@Deprecated("Useful as FlexSample")
object PlantumlPanel : VPanel() {

    var diagramPanel = vPanel(spacing = 3) {
        id = "diagramPanel"
        width = CssSize(100, UNIT.perc)
    }

    val textBox = TextArea(label = "Enter plantuml code here", rows = 20)
    private val okButton = Button("Create Diagram", "fas fa-check", ButtonStyle.SUCCESS).onClick {
        execute()
    }

    const val sampleCode = "\"" +
            "participant BOB [[https://en.wiktionary.org/wiki/best_of_breed]]\\n" +
            "participant PITA [[https://en.wiktionary.org/wiki/PITA]]\\n" +
            "BOB -> PITA: sometimes is a" +
            "\""

    val codePanel = vPanel {
        width = CssSize(400, UNIT.px)
        textBox.value = sampleCode
        add(textBox)
        add(okButton)
    }

    init {
        this.margin = 10.px
        this.minHeight = 400.px

        splitPanel(direction = Direction.VERTICAL) {
            codePanel
            diagramPanel
        }
    }

    private fun execute() {
        UmlUtils.generateDiagram(textBox.value!!, diagramPanel.id!!)
    }

}
