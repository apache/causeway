package org.ro.view


import org.ro.LoginDialog
import org.ro.core.Menu
import pl.treksoft.kvision.dropdown.DropDown
import pl.treksoft.kvision.html.Link
import pl.treksoft.kvision.i18n.I18n.tr
import pl.treksoft.kvision.navbar.Nav.Companion.nav
import pl.treksoft.kvision.navbar.Navbar
import pl.treksoft.kvision.navbar.NavbarType
import pl.treksoft.kvision.utils.px


class RoMenuBar : Navbar() {
    private var link = Link(tr("URL"), icon = "fa-windows").onClick {
        LoginDialog().show()
    }

    private var dropDown = DropDown(
            tr("Main"),
/*            listOf(
                    tr("Basic formatting") to "#!/basic",
                    tr("Forms") to "#!/forms"
            ), */
            icon = "fa-bars",
            forNavbar = true)

    init {
        navbar(type = NavbarType.FIXEDTOP) {
            paddingLeft = 0.px
            nav {
                add(dropDown.add(link))
            }
        }
    }

    private fun invoke() {

    }

    fun amend(menu: Menu) {

    }

    fun setMenu(menu: Menu) {

    }

    fun getMenu(): Menu? {
        return null
    }
}