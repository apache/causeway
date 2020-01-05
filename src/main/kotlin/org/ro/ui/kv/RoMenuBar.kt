package org.ro.ui.kv

import org.ro.core.aggregator.ActionAggregator
import org.ro.core.event.EventStore
import org.ro.to.mb.Menubars
import org.ro.ui.IconManager
import org.ro.ui.Point
import pl.treksoft.kvision.core.CssSize
import pl.treksoft.kvision.core.UNIT
import pl.treksoft.kvision.dropdown.DropDown
import pl.treksoft.kvision.dropdown.ddLink
import pl.treksoft.kvision.dropdown.dropDown
import pl.treksoft.kvision.dropdown.separator
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
            navbar = navbar(label, NavbarType.FIXEDTOP) {
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

            val logTitle = "Log Entries"
            ddLink(
                    logTitle,
                    icon = IconManager.find(logTitle)
            ).onClick {
                val model = EventStore.log
                UiManager.add(logTitle, EventLogTable(model))
            }

            val sampleTitle = "Image Sample"
            ddLink(sampleTitle,
                    icon = IconManager.find(sampleTitle)
            ).onClick {
                val panel = PlantumlPanel() as SimplePanel
                UiManager.add(sampleTitle, panel)
            }
        }
    }

    fun amendMenu(menuBars: Menubars) {
        nav.add(buildFor(menuBars.primary))
        //TODO handle all primaries seperately
        nav.add(buildFor(menuBars.secondary))
        nav.add(buildFor(menuBars.tertiary))
    }

    private fun buildFor(menuEntry: org.ro.to.mb.MenuEntry): DropDown {
        val menu = menuEntry.menu.first()
        val title = menu.named
        val dd = dropDown(
                title,
                icon = IconManager.find(title),
                forNavbar = false,
                style = ButtonStyle.LIGHT)
        menu.section.forEachIndexed { index, section ->
            section.serviceAction.forEach { sa ->
                val label = sa.id!!
                var styles = setOf("text-normal")
                if (IconManager.isDangerous(label)) {
                    styles = setOf("text-danger")
                }
                dd.ddLink(
                        label = label,
                        icon = IconManager.find(label),
                        classes = styles
                ).onClick { _ ->
                    val l = sa.link!!
                    ActionAggregator().invoke(l)
                }
            }
            if (index < menu.section.size - 1) {
                dd.separator()
            }
        }
        return dd
    }

}
