package org.ro.view

import org.ro.core.Menu
import org.ro.core.MenuEntry
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
            paddingLeft = 0.px
            nav {
                add(buildMainEntry())
            }
        }
    }

    private fun buildMainEntry(): DropDown {
        val mainMenu = buildMenuEntry("Main", iconName = "fa-bars")
        val link = Link(tr("URL"), icon = "fa-windows").onClick {
            LoginDialog().show()
        }
        mainMenu.add(link)
        return mainMenu
    }

    fun amendMenu() {
        navbar(type = NavbarType.FIXEDTOP) {
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
        return Link(tr(action), icon = "fa-windows")
    }

    private fun invoke() {

    }

    fun setMenu(menu: Menu) {

    }

    fun getMenu(): Menu? {
        return null
    }
}