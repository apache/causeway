package org.ro.view

import org.ro.core.Menu
import org.ro.core.MenuEntry
import pl.treksoft.kvision.core.CssSize
import pl.treksoft.kvision.core.UNIT
import pl.treksoft.kvision.dropdown.DropDown
import pl.treksoft.kvision.html.Link
import pl.treksoft.kvision.i18n.I18n.tr
import pl.treksoft.kvision.navbar.Nav.Companion.nav
import pl.treksoft.kvision.navbar.Navbar
import pl.treksoft.kvision.navbar.NavbarType
import pl.treksoft.kvision.utils.px

class RoMenuBar : Navbar() {

    init {
        navbar(type = NavbarType.FIXEDTOP) {
            height = CssSize(10, UNIT.mm)
            minHeight = CssSize(10, UNIT.mm)
            maxHeight = CssSize(10, UNIT.mm)
            paddingLeft = 0.px
            nav {
                add(buildMainEntry())
            }
        }
    }

    private fun buildMainEntry(): DropDown {
        val mainMenu = buildMenuEntry("Main", iconName = "fa-eye")  // fa-ankh
        val link = Link(tr("Connect ..."), icon = "fa-server").onClick {
            LoginPrompt().open()
        }
        mainMenu.add(link)
        return mainMenu
    }

    fun amendMenu() {
        navbar(type = NavbarType.FIXEDTOP) {
            height = CssSize(10, UNIT.mm)
            minHeight = CssSize(10, UNIT.mm)
            maxHeight = CssSize(10, UNIT.mm)
            nav {
                add(buildMainEntry())
                for (title: String in Menu.filterUniqueMenuTitles()) {
                    val dd = buildMenuEntry(title)
                    for (me : MenuEntry in Menu.filterEntriesByTitle(title)) { 
                        val menuLink = buildMenuAction(me.action.id)
                        val execLink = me.action.getInvokeLink()!!
                        menuLink.onClick {
                            console.log("[RoMenuBar.amendMenu/Link.invoke] $execLink")
                            execLink.invoke() }
                        
                        dd.add(menuLink)
                    }
                    add(dd)
                }
            }
        }
    }

    private fun buildMenuEntry(title: String, iconName: String? = null): DropDown {
        return DropDown(tr(title), icon = iconName, forNavbar = true)
    }

    private fun buildMenuAction(action: String, iconName: String? = null): Link {
        return Link(tr(action), icon = "fa-bolt")
    }

}