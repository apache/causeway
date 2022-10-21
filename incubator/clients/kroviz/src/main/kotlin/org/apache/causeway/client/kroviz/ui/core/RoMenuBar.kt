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

import io.kvision.core.CssSize
import io.kvision.core.ResString
import io.kvision.core.UNIT
import io.kvision.core.style
import io.kvision.dropdown.DropDown
import io.kvision.dropdown.dropDown
import io.kvision.dropdown.separator
import io.kvision.html.ButtonStyle
import io.kvision.html.Link
import io.kvision.navbar.*
import io.kvision.panel.SimplePanel
import io.kvision.panel.vPanel
import io.kvision.utils.px
import org.apache.causeway.client.kroviz.core.Session
import org.apache.causeway.client.kroviz.core.aggregator.ActionDispatcher
import org.apache.causeway.client.kroviz.core.event.ResourceProxy
import org.apache.causeway.client.kroviz.to.mb.Menubars
import org.apache.causeway.client.kroviz.ui.dialog.About
import org.apache.causeway.client.kroviz.ui.dialog.EventDialog
import org.apache.causeway.client.kroviz.ui.dialog.LoginPrompt
import org.apache.causeway.client.kroviz.ui.dialog.SvgInline
import org.apache.causeway.client.kroviz.ui.menu.DropDownMenuBuilder
import org.apache.causeway.client.kroviz.ui.panel.GeoMap
import org.apache.causeway.client.kroviz.ui.panel.ImageSample
import org.apache.causeway.client.kroviz.ui.panel.SvgMap
import org.apache.causeway.client.kroviz.utils.IconManager
import org.apache.causeway.client.kroviz.utils.Point

class RoMenuBar : SimplePanel() {
    lateinit var navbar: Navbar
    private lateinit var nav: Nav
    private lateinit var mainEntry: DropDown
    private lateinit var mainMenu: DropDown

    init {
        vPanel {
            val label = "" //eventually use for branding
            navbar = navbar(label = label, type = NavbarType.FIXEDTOP) {
                marginLeft = CssSize(-32, UNIT.px)
                height = CssSize(40, UNIT.px)
                nav = nav()
                mainEntry = buildMainMenu()
                nav.add(mainEntry)
            }
        }
    }

    fun add(session: Session) {
        val menuEntry = buildMenuEntryWithImage(
            session.baseUrl,
            image = session.resString,
            { switch(session) })
        if (!mainEntryContains(session.baseUrl)) {
            mainEntry.add(menuEntry)
        }
        switch(session)
    }

    private fun mainEntryContains(baseUrl: String): Boolean {
        val link = findEntryBy(baseUrl)
        return (link != null)
    }

    private fun findEntryBy(baseUrl: String): Link? {
        mainEntry.getChildren().forEach {
            if (it is Link) {
                if (it.label == baseUrl) return it
            }
        }
        return null
    }

    fun switch(session: Session) {
        val resString = session.resString
        if (resString != null) {
            mainEntry.image = session.resString
            mainEntry.icon = null
        }
        mainEntry.image?.apply { systemIconStyle }
        val logEntry = SessionManager.getEventStore().findMenuBarsBy(session.baseUrl)
        if (logEntry != null) {
            val menuBars = logEntry.obj as Menubars
            amendMenu(menuBars)
        }
        ViewManager.setNormalCursor()
    }

    fun updateIcon(session: Session) {
        val resString = session.resString
        mainEntry.image = resString
        val baseUrl = session.baseUrl
        val link = findEntryBy(baseUrl)
        link?.image = resString
    }

    private fun buildMenuEntryWithImage(label: String, image: ResString?, action: dynamic): Link {
        val link = Link(label, image = image, className = "dropdown-item").apply { appIconStyle }
        link.onClick { e ->
            val at = Point(e.pageX.toInt(), e.pageY.toInt())
            ViewManager.position = at
            action()
        }
        return link
    }

    private val systemIconStyle = style(".dropdown-toggle") {
        style("img") {
            height = 20.px
        }
    }

    private val appIconStyle = style(".dropdown-item") {
        style("img") {
            height = 20.px
        }
    }

    private fun buildMenuEntry(label: String, iconName: String, action: dynamic): Link {
        val icon = IconManager.find(iconName)
        val link = Link(label, icon = icon, className = "dropdown-item").onClick { e ->
            val at = Point(e.pageX.toInt(), e.pageY.toInt())
            ViewManager.position = at
            action()
        }
        return link
    }

    private fun buildMainMenu(): DropDown {
        mainMenu = DropDown(
            "",
            icon = IconManager.find("Burger"),
            forNavbar = false,
            style = ButtonStyle.LIGHT
        )
        mainMenu.add(
            buildMenuEntry("Connect ...", "Connect", { LoginPrompt().open() })
        )

        mainMenu.add(
            buildMenuEntry("Toolbar", "Toolbar", { ViewManager.getRoIconBar().toggle() })
        )

        mainMenu.add(
            buildMenuEntry("Events", "Event", { EventDialog().open() })
        )

        val aboutTitle = "About"
        mainMenu.add(
            buildMenuEntry(aboutTitle, "About", { ViewManager.add(aboutTitle, About().dialog) })
        )

        mainMenu.separator()

        val testTitle = "Execute All MenuBar Actions"
        mainMenu.add(
            buildMenuEntry(testTitle, "Test", { this.executeAllMenuBarActions() })
        )

        val stats = "Log Handler Stats"
        mainMenu.add(
            buildMenuEntry(stats, "Console", { this.logStats() })
        )

        mainMenu.separator()

        val subMenu = dropDown("Samples", icon = "fas fa-expand", forDropDown = true)
        val geoMapTitle = "Sample Geo Map"
        subMenu.add(
            buildMenuEntry(geoMapTitle, "Map", { ViewManager.add(geoMapTitle, GeoMap()) })
        )

        val svgMapTitle = "Sample SVG Map"
        subMenu.add(
            buildMenuEntry(svgMapTitle, "Diagram", { ViewManager.add(svgMapTitle, SvgMap()) })
        )

        val svgInlineTitle = "Sample SVG Inline (interactive)"
        subMenu.add(
            buildMenuEntry(svgInlineTitle, "Diagram", { SvgInline().open() })
        )

        val imageTitle = "Sample Image"
        subMenu.add(
            buildMenuEntry(imageTitle, "Image", { ViewManager.add(imageTitle, ImageSample) })
        )

        mainMenu.add(subMenu)

        return mainMenu
    }

    fun addSeparatorToMainMenu() {
        mainMenu.separator()
    }

    fun amendMenu(menuBars: Menubars) {
        resetMenuBar()
        menuBars.primary.menu.forEach { m ->
            val dd = DropDownMenuBuilder.buildForMenu(m)
            if (dd.getChildren().isNotEmpty()) nav.add(dd)
        }
        nav.add(DropDownMenuBuilder.buildForMenu(menuBars.secondary.menu.first()))
        nav.add(DropDownMenuBuilder.buildForMenu(menuBars.tertiary.menu.first()))
    }

    // this empties out any existing menuItems (non-system)
    private fun resetMenuBar() {
        nav.removeAll()
        nav.add(mainMenu)
    }

    fun executeAllMenuBarActions() {
        val menuBars = SessionManager.getEventStore().findMenuBars()!!.obj as Menubars
        menuBars.primary.menu.forEach { m ->
            m.section.forEachIndexed { index, section ->
                section.serviceAction.forEach { sa ->
                    ResourceProxy().fetch(sa.link!!, ActionDispatcher())
                }
            }
        }
    }

    fun logStats() {
        SessionManager.responseHandlerStatistics.forEach {
            console.log("${it.key} -> ${it.value}")
        }
    }

}
