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
package org.apache.isis.client.kroviz.ui.core

import org.apache.isis.client.kroviz.core.event.EventState
import org.apache.isis.client.kroviz.core.event.LogEntry
import org.apache.isis.client.kroviz.core.model.DiagramDM
import org.apache.isis.client.kroviz.ui.dialog.DiagramDialog
import org.apache.isis.client.kroviz.ui.dialog.NotificationDialog
import org.apache.isis.client.kroviz.ui.diagram.ClassDiagram
import org.apache.isis.client.kroviz.utils.IconManager
import io.kvision.core.*
import io.kvision.html.Button
import io.kvision.html.ButtonStyle
import io.kvision.navbar.Nav
import io.kvision.navbar.Navbar
import io.kvision.navbar.NavbarType
import io.kvision.panel.SimplePanel

object RoStatusBar {
    val navbar = Navbar(
            type = NavbarType.FIXEDBOTTOM,
            classes = setOf("status-bar"))
    private val nav = Nav(rightAlign = true)
    private val userBtn: Button = buildButton("", "Me", ButtonStyle.OUTLINEWARNING)
    private val classDiagram = buildButton("", "Diagram", ButtonStyle.OUTLINEWARNING)
    private val lastError = buildButton("OK", "OK", ButtonStyle.OUTLINESUCCESS)
    private val alert = buildButton("", "Notification", ButtonStyle.OUTLINESUCCESS)

    private fun buildButton(text: String, iconName: String, style: ButtonStyle): Button {
        return Button(
                text = text,
                icon = IconManager.find(iconName),
                style = style).apply {
            padding = CssSize(-16, UNIT.px)
            margin = CssSize(0, UNIT.px)
        }
    }

    init {
        navbar.add(nav)
        nav.add(lastError)
        nav.add(classDiagram)
        nav.add(userBtn)
        nav.add(alert)
    }

    fun updateDiagram(dd: DiagramDM) {
        classDiagram.style = ButtonStyle.OUTLINESUCCESS
        classDiagram.onClick {
            val title = dd.title
            val code = ClassDiagram.build(dd)
            DiagramDialog(title, code).open()
        }
    }

    fun updateUser(user: String) {
        userBtn.setAttribute(name = "title", value = user)
        userBtn.style = ButtonStyle.OUTLINESUCCESS
    }

    private fun notify(text: String) {
        alert.setAttribute(name = "title", value = text)
        alert.style = ButtonStyle.OUTLINEDANGER
        alert.onClick {
            NotificationDialog(text).open()
        }
    }

    fun acknowledge() {
        alert.setAttribute(name = "title", value = "no new notifications")
        alert.style = ButtonStyle.OUTLINELIGHT
    }

    fun update(le: LogEntry?) {
        when (le?.state) {
            EventState.ERROR -> turnRed(le)
            EventState.MISSING -> turnRed(le)
            else -> turnGreen(nav)
        }
    }

    private fun turnGreen(panel: SimplePanel) {
        panel.removeCssClass(IconManager.DANGER)
        panel.removeCssClass(IconManager.WARN)
        panel.addCssClass(IconManager.OK)
        navbar.background = Background(color = Color.name(Col.LIGHTGRAY))
    }

    private fun turnRed(logEntry: LogEntry) {
        var text = logEntry.url
        if (text.length > 50) text = text.substring(0, 49)
        lastError.text = text
        lastError.style = ButtonStyle.OUTLINEDANGER
        lastError.icon = IconManager.find("Error")
        notify(text)
    }

}
