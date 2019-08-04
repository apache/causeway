package org.ro.view

import org.ro.core.Menu
import org.ro.core.MenuEntry
import org.ro.core.event.EventStore
import org.ro.org.ro.core.observer.ActionObserver
import org.ro.org.ro.view.table.TableFactory
import org.ro.view.table.DynamicTable
import org.ro.view.table.el.EventLogTable
import pl.treksoft.kvision.core.CssSize
import pl.treksoft.kvision.core.UNIT
import pl.treksoft.kvision.dropdown.DropDown
import pl.treksoft.kvision.html.Link
import pl.treksoft.kvision.i18n.I18n.tr
import pl.treksoft.kvision.navbar.Nav
import pl.treksoft.kvision.navbar.Navbar
import pl.treksoft.kvision.navbar.NavbarType

@ExperimentalUnsignedTypes
object RoMenuBar {
    val leftMargin = CssSize(-12, UNIT.px)
    var navbar: Navbar
    var nav: Nav

    init {
        navbar = Navbar(type = NavbarType.FIXEDTOP)
        navbar.marginLeft = leftMargin
        nav = Nav()
        navbar.add(nav)
        val mainEntry = buildMainEntry()
        nav.add(mainEntry)
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
            RoView.addTab(tr(title), EventLogTable(model))
        }
        mainMenu.add(log)

        val sample = "Dynamic Table"
        val dynTable = createLink(title).onClick {
            val model = TableFactory().testData()
            RoView.addTab(tr(sample), DynamicTable(model))
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
            for (me: MenuEntry in Menu.filterEntriesByTitle(title)) {
                val menuLink = buildMenuAction(me.action.id)
                val execLink = me.action.getInvokeLink()!!
                menuLink.onClick {
                    console.log("[RoMenuBar.amendMenu/Link.invoke] $execLink")
                    ActionObserver().invoke(execLink)
                }
                dd.add(menuLink)
            }
            nav.add(dd)
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
