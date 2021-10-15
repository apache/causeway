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
import io.kvision.core.UNIT
import io.kvision.dropdown.DropDown
import io.kvision.html.Button
import io.kvision.html.ButtonStyle
import io.kvision.html.Link
import io.kvision.navbar.*
import io.kvision.panel.SimplePanel
import io.kvision.panel.vPanel
import kotlinx.browser.window
import org.apache.isis.client.kroviz.core.event.EventStore
import org.apache.isis.client.kroviz.core.event.ReplayCommand
import org.apache.isis.client.kroviz.to.mb.Menubars
import org.apache.isis.client.kroviz.ui.chart.SampleChartModel
import org.apache.isis.client.kroviz.ui.dialog.About
import org.apache.isis.client.kroviz.ui.dialog.LoginPrompt
import org.apache.isis.client.kroviz.ui.dialog.SvgInline
import org.apache.isis.client.kroviz.ui.panel.*
import org.apache.isis.client.kroviz.utils.IconManager
import org.apache.isis.client.kroviz.utils.Point

class RoMenuBar : SimplePanel() {
    lateinit var navbar: Navbar
    private lateinit var nav: Nav

    init {
        vPanel {
            val label = "" //IMPROVE use for branding
            navbar = navbar(label = label, type = NavbarType.FIXEDTOP) {
                marginLeft = CssSize(-32, UNIT.px)
                height = CssSize(40, UNIT.px)
                nav = nav()
//                logoButton() leaves an empty space here without network connection
                val mainEntry = buildMainMenu()
                nav.add(mainEntry)
            }
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
        val mainMenu = DropDown(
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
            buildMenuEntry("Replay", "Replay", { ReplayCommand().execute() })
        )

        mainMenu.add(
            buildMenuEntry("History", "History", { UiManager.add("Log Entries", EventLogTable(EventStore.log)) })
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
            buildMenuEntry(aboutTitle, "Info", { UiManager.add(aboutTitle, About().open()) })
        )

        console.log("[RMB.buildMainMenu]")
        console.log(mainMenu)
        console.log(mainMenu.getChildren())
        return mainMenu
    }

    fun amendMenu(menuBars: Menubars) {
        //       logoButton()
        menuBars.primary.menu.forEach { m ->
            val dd = MenuFactory.buildForMenu(m)
            if (dd.getChildren().isNotEmpty()) nav.add(dd)
        }
        nav.add(MenuFactory.buildForMenu(menuBars.secondary.menu.first()))
        nav.add(MenuFactory.buildForMenu(menuBars.tertiary.menu.first()))
    }

    private fun logoButton() {
        val classNames = "isis-logo-button-image logo-button"
        val logo = Button("", style = ButtonStyle.LINK)
        logo.addCssClass(classNames)
        logo.onClick {
            window.open("https://isis.apache.org")
        }
        nav.add(logo)
    }

}
