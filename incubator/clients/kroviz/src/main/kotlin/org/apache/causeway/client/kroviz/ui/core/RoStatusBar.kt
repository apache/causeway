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
package org.apache.causeway.client.kroviz.ui.core

import io.kvision.core.*
import io.kvision.html.Button
import io.kvision.html.ButtonStyle
import io.kvision.navbar.Nav
import io.kvision.navbar.Navbar
import io.kvision.navbar.NavbarType
import io.kvision.panel.SimplePanel
import kotlinx.browser.window
import org.apache.causeway.client.kroviz.core.event.EventState
import org.apache.causeway.client.kroviz.core.event.LogEntry
import org.apache.causeway.client.kroviz.core.event.StatusPo
import org.apache.causeway.client.kroviz.core.model.DiagramDM
import org.apache.causeway.client.kroviz.ui.diagram.ClassDiagram
import org.apache.causeway.client.kroviz.ui.dialog.DiagramDialog
import org.apache.causeway.client.kroviz.ui.dialog.EventDialog
import org.apache.causeway.client.kroviz.ui.dialog.NotificationDialog
import org.apache.causeway.client.kroviz.utils.IconManager

class RoStatusBar {
    val navbar = Navbar(type = NavbarType.FIXEDBOTTOM)

    private val nav = Nav(rightAlign = true)
    private val userBtn: Button = buildButton("", "Me", ButtonStyle.OUTLINEWARNING)
    private val classDiagram = buildButton("", "Diagram", ButtonStyle.OUTLINEWARNING)
    private val success = buildButton("0", "OK", ButtonStyle.OUTLINESUCCESS)
    private val running = buildButton("0", "Run", ButtonStyle.OUTLINEWARNING)
    private val errors = buildButton("0", "Error", ButtonStyle.OUTLINEDANGER)
    private val views = buildButton("0", "Visualize", ButtonStyle.LIGHT)
    private val dialogs = buildButton("0", "Dialog", ButtonStyle.LIGHT)

    private fun buildButton(text: String, iconName: String, style: ButtonStyle): Button {
        return Button(
            text = text,
            icon = IconManager.find(iconName),
            style = style
        ).apply {
            padding = CssSize(-16, UNIT.px)
            margin = CssSize(0, UNIT.px)
        }
    }

    init {
        navbar.addCssClass("status-bar")
        navbar.add(nav)
//        nav.add(causewayButton())
//        nav.add(kvisionButton())
        nav.add(success)
        nav.add(running)
        nav.add(errors)
        nav.add(views)
        nav.add(dialogs)
        nav.add(userBtn)
        initRunning()
        initErrors()
        initViews()
        initDialogs()
    }

    fun update(status: StatusPo) {
        success.text = status.successCnt.toString()
        running.text = status.runningCnt.toString()
        errors.text = status.errorCnt.toString()
        views.text = status.viewsCnt.toString()
        dialogs.text = status.dialogsCnt.toString()
    }

    private fun initRunning() {
        running.setAttribute(name = "title", value = "Number of Requests in State RUNNING")
        running.onClick {
            EventDialog(EventState.RUNNING).open()
        }
    }

    private fun initErrors() {
        errors.setAttribute(name = "title", value = "Number of Requests in State ERROR")
        errors.onClick {
            EventDialog(EventState.ERROR).open()
        }
    }

    private fun initViews() {
        views.setAttribute(name = "title", value = "Number of VIEWS")
        views.onClick {
            EventDialog(EventState.VIEW).open()
        }
    }

    private fun initDialogs() {
        views.setAttribute(name = "title", value = "Number of DIALOGS")
        views.onClick {
            EventDialog(EventState.DIALOG).open()
        }
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
        views.setAttribute(name = "title", value = text)
        views.style = ButtonStyle.OUTLINEDANGER
        views.onClick {
            NotificationDialog(text).open()
        }
    }

    fun acknowledge() {
        views.setAttribute(name = "title", value = "no new notifications")
        views.style = ButtonStyle.OUTLINELIGHT
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
        errors.text = text
        errors.style = ButtonStyle.OUTLINEDANGER
        errors.icon = IconManager.find("Error")
        notify(text)
    }

    private fun causewayButton(): Button {
        val classes = "causeway-logo-button-image logo-button"
        val b = Button("", style = ButtonStyle.LINK)
        b.addCssClass(classes)
        return b.onClick {
            window.open("https://causeway.apache.org")
        }
    }

    private fun kvisionButton(): Button {
        val classes = "kvision-logo-button-image logo-button"
        val b = Button("", style = ButtonStyle.LINK)
        b.addCssClass(classes)
        return b.onClick {
            window.open("https://kvision.io")
        }
    }

    /*
    http://tabulator.info/images/tabulator_favicon_simple.png
    http://tabulator.info/images/tabulator_small.png

    https://kroki.io/assets/logo.svg
     */

}
