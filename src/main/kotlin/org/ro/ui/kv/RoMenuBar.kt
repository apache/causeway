package org.ro.ui.kv

import org.ro.core.Menu
import org.ro.core.MenuEntry
import org.ro.ui.kv.UiManager
import org.ro.core.aggregator.ActionAggregator
import org.ro.core.event.EventStore
import org.ro.ui.IconManager
import pl.treksoft.kvision.core.CssSize
import pl.treksoft.kvision.core.UNIT
import pl.treksoft.kvision.dropdown.DropDown
import pl.treksoft.kvision.html.Link
import pl.treksoft.kvision.i18n.I18n.tr
import pl.treksoft.kvision.navbar.Nav
import pl.treksoft.kvision.navbar.Navbar
import pl.treksoft.kvision.navbar.NavbarType

object RoMenuBar {
    var navbar: Navbar
    private val nav = Nav()

    init {
        navbar = Navbar(type = NavbarType.FIXEDTOP) {
            marginLeft = CssSize(-12, UNIT.px)
            add(nav)
            val mainEntry = buildMainEntry()
            nav.add(mainEntry)
        }
    }

    private fun buildMainEntry(): DropDown {
        val mainMenu = buildMenuEntry("", iconName = "fa-bars")

        val link = createLink("Connect ...").onClick {
            LoginPrompt().open()
        }
        mainMenu.add(link)

        val title = "Log Entries"
        val log = createLink(title).onClick {
            val model = EventStore.log
            UiManager.add(title, EventLogTable(model))
        }
        mainMenu.add(log)

        val sample = "Image Sample"
        val dynTable = createLink(sample).onClick {
            val panel = ImagePanel()
            UiManager.add(sample, panel)
        }
        mainMenu.add(dynTable)

        return mainMenu
    }

    private fun createLink(title: String): Link {
        val icon = IconManager.find(title)
        val link = Link(tr(title), icon = icon)
        return link
    }

    fun amendMenu() {
        for (title: String in Menu.filterUniqueMenuTitles()) {
            val dd = buildMenuEntry(title)
            nav.add(dd)
            for (me: MenuEntry in Menu.filterEntriesByTitle(title)) {
                val menuLink = buildMenuAction(me.action.id)
                dd.add(menuLink)
                val l = me.action.getInvokeLink()!!
                menuLink.onClick {
                    ActionAggregator().invoke(l)
                }
            }
        }
    }

    private fun buildMenuEntry(title: String, iconName: String? = null): DropDown {
        val label = tr(title)
        val icon = iconName ?: IconManager.find(title)
        return DropDown(label, icon = icon, forNavbar = true)
    }

    private fun buildMenuAction(action: String, iconName: String? = null): Link {
        val label = tr(action)
        val icon = iconName ?: IconManager.find(action)
        return Link(label, icon = icon)
    }

}
