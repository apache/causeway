package org.apache.isis.client.kroviz.ui.kv

import org.apache.isis.client.kroviz.core.event.EventStore
import org.apache.isis.client.kroviz.to.mb.Menubars
import org.apache.isis.client.kroviz.ui.ExportDialog
import org.apache.isis.client.kroviz.utils.IconManager
import org.apache.isis.client.kroviz.utils.Point
import pl.treksoft.kvision.core.CssSize
import pl.treksoft.kvision.core.UNIT
import pl.treksoft.kvision.dropdown.DropDown
import pl.treksoft.kvision.dropdown.ddLink
import pl.treksoft.kvision.dropdown.dropDown
import pl.treksoft.kvision.html.Button
import pl.treksoft.kvision.html.ButtonStyle
import pl.treksoft.kvision.navbar.*
import pl.treksoft.kvision.panel.SimplePanel
import pl.treksoft.kvision.panel.vPanel
import kotlin.browser.window

object RoMenuBar : SimplePanel() {
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

    private fun buildMainMenu(): DropDown {
        return dropDown(
                "",
                icon = IconManager.find("Burger"),
                forNavbar = false,
                style = ButtonStyle.LIGHT)
        {
            ddLink(
                    "Connect ...",
                    icon = IconManager.find("Connect")
            ).onClick { e ->
                val at = Point(e.pageX.toInt(), e.pageY.toInt())
                LoginPrompt().open(at)
            }

            val toolTitle = "Toolbar"
            ddLink(toolTitle,
                    icon = IconManager.find(toolTitle)
            ).onClick {
                RoIconBar.toggle()
            }

            val sampleTitle = "History"
            ddLink(sampleTitle,
                    icon = IconManager.find(sampleTitle)
            ).onClick {
                val model = EventStore.log
                UiManager.add("Log Entries", EventLogTable(model))
            }

            val exportTitle = "Export Events for Replay"
            ddLink(exportTitle,
                    icon = IconManager.find("Export")
            ).onClick {
                ExportDialog().open()
            }

            val chartTitle = "Sample Chart"
            ddLink(chartTitle,
                    icon = IconManager.find("Chart")
            ).onClick {
                UiManager.add(chartTitle, ChartTab())
            }
        }
    }

    fun amendMenu(menuBars: Menubars) {
        logoButton()
        menuBars.primary.menu.forEach { m ->
            val dd = MenuFactory.buildFor(m)
            if (dd.getChildren().isNotEmpty()) nav.add(dd)
        }
        nav.add(MenuFactory.buildFor(menuBars.secondary.menu.first()))
        nav.add(MenuFactory.buildFor(menuBars.tertiary.menu.first()))
    }

    private fun logoButton() {
        val classes = setOf("logo-button-image", "logo-button")
        val logo = Button("", style = ButtonStyle.LINK, classes = classes)
                .onClick {
                    window.open("https://isis.apache.org")
                }
        nav.add(logo)
    }

}
