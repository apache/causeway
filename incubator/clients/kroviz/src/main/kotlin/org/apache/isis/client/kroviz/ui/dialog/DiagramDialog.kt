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

import org.apache.isis.client.kroviz.to.ValueType
import org.apache.isis.client.kroviz.ui.core.*
import org.apache.isis.client.kroviz.utils.*
import io.kvision.html.Link as KvisionHtmlLink

class DiagramDialog(
        var label: String,
        private var pumlCode: String
) : Controller() {

    private var callBack: Any = UUID()
    private val formItems = mutableListOf<FormItem>()

    override fun open() {
        super.open()
        UmlUtils.generateJsonDiagram(pumlCode, callBack)
    }

    init {
        val fi = FormItem("svg", ValueType.SVG_INLINE, callBack = callBack)
        formItems.add(fi)

        dialog = RoDialog(
                widthPerc = 80,
                heightPerc = 80,
                caption = label,
                items = formItems,
                controller = this,
                defaultAction = "Pin",
                menu = buildMenu()
        )
    }

    override fun execute(action: String?) {
        pin()
    }

    private fun pin() {
        val svgCode = getDiagramCode()
        UiManager.addSvg("Diagram", svgCode)
        dialog.close()
    }

    private fun getDiagramCode(): String {
        val logEntry = SessionManager.getEventStore().findByDispatcher(callBack as UUID)
        return logEntry.getResponse()
    }

    @Deprecated("use leaflet/svg")
    fun scale(direction: Direction) {
        val svgCode = getDiagramCode()
        val svg = ScalableVectorGraphic(svgCode)
        when (direction) {
            Direction.UP -> svg.scaleUp()
            Direction.DOWN -> svg.scaleDown()
        }
        DomUtil.replaceWith(callBack as UUID, svg)
    }

    private fun buildMenu(): List<KvisionHtmlLink> {
        val menu = mutableListOf<KvisionHtmlLink>()
        menu.add(buildPinAction())
        menu.add(buildDownloadAction())
        return menu
    }

    private fun buildPinAction(): io.kvision.html.Link {
        val action = MenuFactory.buildActionLink(
                label = "Pin",
                menuTitle = "Pin")
        action.onClick {
            pin()
        }
        return action
    }

    private fun buildDownloadAction(): io.kvision.html.Link {
        val action = MenuFactory.buildActionLink(
                label = "Download",
                menuTitle = "Download")
        action.onClick {
            download()
        }
        return action
    }

    private fun download() {
        val svgCode = getDiagramCode()
        DownloadDialog(fileName = "diagram.svg", svgCode).open()
        dialog.close()
    }

}
