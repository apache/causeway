package org.ro.ui

import org.ro.core.event.LogEntry
import pl.treksoft.kvision.core.CssSize
import pl.treksoft.kvision.core.UNIT
import pl.treksoft.kvision.navbar.Nav
import pl.treksoft.kvision.navbar.Navbar
import pl.treksoft.kvision.navbar.NavbarType
import pl.treksoft.kvision.navbar.navLink

object RoStatusBar {
    val navbar: Navbar
    private val nav = Nav()
    private val urlLink = nav.navLink("", icon = "fab fa-windows")
    private var userLink = nav.navLink("", icon = "far fa-user")


    init {
        navbar = Navbar(type = NavbarType.FIXEDBOTTOM) {
            height = CssSize(8, UNIT.mm)
            minHeight = CssSize(8, UNIT.mm)
        }
        navbar.add(nav)
        nav.add(urlLink)
        nav.add(userLink)
    }

    fun brand(colorCode: String) {
        navbar.setAttribute(name = "color", value = "#00FF00")
    }

    fun updateUser(user: String) {
        userLink.setAttribute(name = "title", value = user)
    }

    fun update(le: LogEntry?) {
        val url = le?.title!!
        urlLink.setAttribute(name = "title", value = url)
        urlLink.setAttribute(name = "content", value = url)
    }

}
