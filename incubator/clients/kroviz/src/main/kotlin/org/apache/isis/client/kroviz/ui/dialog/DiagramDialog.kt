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

import io.kvision.panel.VPanel
import org.apache.isis.client.kroviz.core.event.EventStore
import org.apache.isis.client.kroviz.to.ValueType
import org.apache.isis.client.kroviz.ui.core.*
import org.apache.isis.client.kroviz.utils.*
import io.kvision.html.Link as KvisionHtmlLink

class DiagramDialog(
        var label: String,
        private var pumlCode: String
) : Command() {

    private var callBack: Any = UUID()
    private var dialog: RoDialog
    private val formItems = mutableListOf<FormItem>()

    fun open() {
        dialog.open()
        UmlUtils.generateJsonDiagram(pumlCode, callBack)
    }

    init {
        val fi = FormItem("svg", ValueType.SVG_INLINE, callBack = callBack)
        formItems.add(fi)

        dialog = RoDialog(
                widthPerc = 80,
                caption = "Diagram",
                items = formItems,
                command = this,
                defaultAction = "Pin",
                menu = buildMenu()
        )
    }

    override fun execute() {
        pin()
    }

    private fun pin() {
        val newUuid = UUID()
        val newSvg= getDiagram(newUuid)
        val panel = buildNewPanel(newUuid)
        console.log("[DD.pin]")
        console.log(panel)
        console.log(panel.panel!!.getChildren().first())
        UiManager.add("Diagram", panel, newSvg)
        DomUtil.replaceWith(newUuid, newSvg)
        dialog.close()
    }

    private fun getDiagram(newUuid:UUID?): ScalableVectorGraphic {
        val logEntry = EventStore.findByDispatcher(callBack as UUID)
        val svgStr = logEntry.getResponse()
        return ScalableVectorGraphic(svgStr, newUuid)
    }

    private fun buildNewPanel(newUuid: UUID): FormPanelFactory {
        val formItems = mutableListOf<FormItem>()
        val newFi = FormItem("svg", ValueType.SVG_INLINE, callBack = newUuid)
        formItems.add(newFi)
        return FormPanelFactory(formItems)
    }

    @Deprecated("use leaflet/svg")
    fun scale(direction: Direction) {
        val svg = getDiagram(null)
        when (direction) {
            Direction.UP -> svg.scaleUp()
            Direction.DOWN -> svg.scaleDown()
        }
        DomUtil.replaceWith(callBack as UUID, svg)
    }

    fun buildMenu(): List<KvisionHtmlLink> {
        val menu = mutableListOf<KvisionHtmlLink>()
        val action = MenuFactory.buildActionLink(
                label = "Pin",
                menuTitle = "pin")
        action.onClick {
            pin()
        }
        menu.add(action)
        return menu
    }

}
