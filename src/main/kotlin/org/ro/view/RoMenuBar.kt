package org.ro.view

import org.ro.core.Menu
import org.ro.core.MenuEntry
import org.ro.view.table.el.EventLogTab
import org.ro.view.table.el.EventLogTable
import org.ro.view.table.el.EventLogTable2
import pl.treksoft.kvision.core.CssSize
import pl.treksoft.kvision.core.UNIT
import pl.treksoft.kvision.dropdown.DropDown
import pl.treksoft.kvision.html.Link
import pl.treksoft.kvision.i18n.I18n.tr
import pl.treksoft.kvision.navbar.Nav.Companion.nav
import pl.treksoft.kvision.navbar.Navbar
import pl.treksoft.kvision.navbar.NavbarType

class RoMenuBar : Navbar() {
    val leftMargin = CssSize(-12, UNIT.px)

    init {
        navbar(type = NavbarType.FIXEDTOP) {
            marginLeft = leftMargin
            nav {
                add(buildMainEntry())
            }
        }
    }

    private fun buildMainEntry(): DropDown {
        val mainMenu = buildMenuEntry("", iconName = "fa-bars")

        var title = "Connect ..."
        var icon = IconManager.find(title)
        val link = Link(tr(title), icon = icon).onClick {
            LoginPrompt().open()
        }
        mainMenu.add(link)

        title = "Log Entries"
        icon = IconManager.find(title)
        val log = Link(tr(title), icon = icon).onClick {
            val tableSpec = EventLogTab().csList
            RoView.addTab(tr(title), EventLogTable(tableSpec), icon)
        }
        mainMenu.add(log)

        title = "Log Entries 2"
        icon = IconManager.find(title)
        val log2 = Link(tr(title), icon = icon).onClick {
            val tableSpec = EventLogTab().csList
            RoView.addTab(tr(title), EventLogTable2(), icon)
        }
        mainMenu.add(log2)

        return mainMenu
    }

    fun amendMenu() {
        navbar(type = NavbarType.FIXEDTOP) {
            marginLeft = leftMargin
            nav {
                add(buildMainEntry())
                for (title: String in Menu.filterUniqueMenuTitles()) {
                    val dd = buildMenuEntry(title)
                    for (me: MenuEntry in Menu.filterEntriesByTitle(title)) {
                        val menuLink = buildMenuAction(me.action.id)
                        val execLink = me.action.getInvokeLink()!!
                        menuLink.onClick {
                            console.log("[RoMenuBar.amendMenu/Link.invoke] $execLink")
                            execLink.invoke()
                        }
                        dd.add(menuLink)
                    }
                    add(dd)
                }
            }
        }
    }

    fun amendMenuNew() {
        this.nav {
            for (title: String in Menu.filterUniqueMenuTitles()) {
                val dd = buildMenuEntry(title)
                for (me: MenuEntry in Menu.filterEntriesByTitle(title)) {
                    val menuLink = buildMenuAction(me.action.id)
                    val execLink = me.action.getInvokeLink()!!
                    menuLink.onClick {
                        console.log("[RoMenuBar.amendMenu/Link.invoke] $execLink")
                        execLink.invoke()
                    }
                    dd.add(menuLink)
                }
                add(dd)
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