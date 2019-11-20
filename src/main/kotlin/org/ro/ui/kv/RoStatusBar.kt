package org.ro.ui

import org.ro.core.event.LogEntry
import pl.treksoft.kvision.core.CssSize
import pl.treksoft.kvision.core.UNIT
import pl.treksoft.kvision.navbar.*

object RoStatusBar {
    val navbar: Navbar = Navbar(type = NavbarType.FIXEDBOTTOM) {
        height = CssSize(8, UNIT.mm)
        minHeight = CssSize(8, UNIT.mm)
    }
    private val nav = Nav()
    private val urlLink = nav.navLink("", icon = "fab fa-windows")
    private val userLink = nav.navLink("", icon = "far fa-user")


    init {
        navbar.add(nav)
        nav.add(urlLink)
        nav.add(userLink)
    }

    fun brand(colorCode: String) {
        navbar.setAttribute(name = "color", value = colorCode)
        navbar.nColor = NavbarColor.DARK //IMPROVE changing attributes a runtime possible?
    }

    fun updateUser(user: String) {
        userLink.setAttribute(name = "title", value = user)
    }

    fun update(le: LogEntry?) {
        val url = le?.title!!
        urlLink.setAttribute(name = "title", value = url)
    }

}
