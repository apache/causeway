package org.ro.ui.kv

import org.ro.core.event.EventStore
import org.ro.to.mb.Menubars
import org.ro.ui.ExportAlert
import org.ro.ui.IconManager
import org.ro.ui.Point
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

object RoMenuBar : SimplePanel() {
    lateinit var navbar: Navbar
    lateinit var nav: Nav

    init {
        vPanel() {
            val label = "" //IMPROVE use for branding
            navbar = navbar(label = label, type = NavbarType.FIXEDTOP) {
                marginLeft = CssSize(-32, UNIT.px)
                height = CssSize(40, UNIT.px)
                nav = nav()
                val mainEntry = buildMainMenu()
                nav.add(mainEntry)
            }
        }
    }

    private fun buildMainMenu(): DropDown {
        val iconName = IconManager.find("Burger") //IMPROVE use for branding
        return dropDown(
                "",
                icon = iconName,
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
                ExportAlert().open()
            }
        }
    }

    fun amendMenu(menuBars: Menubars) {
        logoButton()
        menuBars.primary.menu.forEach { m ->
            nav.add(MenuFactory.buildFor(m))
        }
        //TODO handle secondary / tertiary as well
        nav.add(MenuFactory.buildFor(menuBars.secondary.menu.first()))
        nav.add(MenuFactory.buildFor(menuBars.tertiary.menu.first()))
    }

    private fun logoButton() {
        val classes = setOf("logo-button-image", "logo-button")
        val logo = Button("", style = ButtonStyle.LINK, classes = classes)
        nav.add(logo)
    }

}
