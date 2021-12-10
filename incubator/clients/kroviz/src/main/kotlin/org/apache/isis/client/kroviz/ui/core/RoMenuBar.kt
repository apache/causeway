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

import io.kvision.core.CssSize
import io.kvision.core.ResString
import io.kvision.core.UNIT
import io.kvision.core.style
import io.kvision.dropdown.DropDown
import io.kvision.dropdown.separator
import io.kvision.html.ButtonStyle
import io.kvision.html.Link
import io.kvision.navbar.*
import io.kvision.panel.SimplePanel
import io.kvision.panel.vPanel
import io.kvision.utils.px
import org.apache.isis.client.kroviz.core.Session
import org.apache.isis.client.kroviz.core.event.EventStore
import org.apache.isis.client.kroviz.to.mb.Menubars
import org.apache.isis.client.kroviz.ui.chart.SampleChartModel
import org.apache.isis.client.kroviz.ui.dialog.*
import org.apache.isis.client.kroviz.ui.panel.EventChart
import org.apache.isis.client.kroviz.ui.panel.GeoMap
import org.apache.isis.client.kroviz.ui.panel.ImageSample
import org.apache.isis.client.kroviz.ui.panel.SvgMap
import org.apache.isis.client.kroviz.utils.IconManager
import org.apache.isis.client.kroviz.utils.Point

class RoMenuBar : SimplePanel() {
    lateinit var navbar: Navbar
    private lateinit var nav: Nav
    private lateinit var mainEntry: DropDown
    private lateinit var mainMenu: DropDown

    init {
        vPanel {
            val label = "" //IMPROVE use for branding
            navbar = navbar(label = label, type = NavbarType.FIXEDTOP) {
                marginLeft = CssSize(-32, UNIT.px)
                height = CssSize(40, UNIT.px)
                nav = nav()
                mainEntry = buildMainMenu()
                nav.add(mainEntry)
            }
        }
    }

    private fun testFirstSession() {
        mainEntry.separator()
        val session = SessionManager.getSession()
        insertSession(session)
    }

    private fun insertSession(session:Session) {
        val menuEntry = buildMenuEntryWithImage(
            session.baseUrl,
            image = session.resString,
            { this.switch(session) })
        mainEntry.add(menuEntry)
    }

    private fun switch(session: Session) {
        mainEntry.image = session.resString
        mainEntry.icon = null
        mainEntry.image.apply { systemIconStyle }
    }

    private fun buildMenuEntryWithImage(label: String, image: ResString?, action: dynamic): Link {
        val link = Link(label, image = image, className = "dropdown-item").apply { appIconStyle }
        link.onClick { e ->
            val at = Point(e.pageX.toInt(), e.pageY.toInt())
            UiManager.position = at
            action()
        }
        return link
    }

    val systemIconStyle = style(".dropdown-toggle") {
        style("img") {
            height = 20.px
        }
    }
    val appIconStyle = style(".dropdown-item") {
        style("img") {
            height = 20.px
        }
    }

    private fun buildMenuEntry(label: String, iconName: String, action: dynamic): Link {
        val icon = IconManager.find(iconName)
        val link = Link(label, icon = icon, className = "dropdown-item").onClick { e ->
            val at = Point(e.pageX.toInt(), e.pageY.toInt())
            UiManager.position = at
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
            buildMenuEntry("Toolbar", "Toolbar", { UiManager.getRoIconBar().toggle() })
        )

        mainMenu.add(
            buildMenuEntry("Events", "Event", { EventDialog().open() })
        )

        val chartTitle = "Sample Chart"
        mainMenu.add(
            buildMenuEntry(chartTitle, "Chart", { UiManager.add(chartTitle, EventChart(SampleChartModel())) })
        )

        val geoMapTitle = "Sample Geo Map"
        mainMenu.add(
            buildMenuEntry(geoMapTitle, "Map", { UiManager.add(geoMapTitle, GeoMap()) })
        )

        val svgMapTitle = "Sample SVG Map"
        mainMenu.add(
            buildMenuEntry(svgMapTitle, "Diagram", { UiManager.add(svgMapTitle, SvgMap()) })
        )

        val svgInlineTitle = "Sample SVG Inline (interactive)"
        mainMenu.add(
            buildMenuEntry(svgInlineTitle, "Diagram", { SvgInline().open() })
        )

        val imageTitle = "Sample Image"
        mainMenu.add(
            buildMenuEntry(imageTitle, "Image", { UiManager.add(imageTitle, ImageSample) })
        )

        val aboutTitle = "About"
        mainMenu.add(
            buildMenuEntry(aboutTitle, "Info", { UiManager.add(aboutTitle, About().dialog) })
        )

        val testTitle = "Test"
        mainMenu.add(
            buildMenuEntry(testTitle, "Test", { this.testFirstSession() })
        )

        mainMenu.add(
            buildMenuEntry("Browser in IFrame", "Wikipedia", { BrowserWindow("https://isis.apache.org/").open() })
        )

        mainMenu.add(
            buildMenuEntry("SSH", "Terminal", { ShellWindow("localhost:8080").open() })
        )

        return mainMenu
    }

    fun amendMenu(menuBars: Menubars) {
        menuBars.primary.menu.forEach { m ->
            val dd = MenuFactory.buildForMenu(m)
            if (dd.getChildren().isNotEmpty()) nav.add(dd)
        }
        nav.add(MenuFactory.buildForMenu(menuBars.secondary.menu.first()))
        nav.add(MenuFactory.buildForMenu(menuBars.tertiary.menu.first()))
    }

}
