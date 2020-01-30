package org.ro.ui

import org.ro.core.event.EventStore
import org.ro.core.event.LogEntry
import org.ro.ui.kv.EventLogTable
import org.ro.ui.kv.UiManager
import pl.treksoft.kvision.core.CssSize
import pl.treksoft.kvision.core.UNIT
import pl.treksoft.kvision.navbar.*

object RoStatusBar {
    val navbar: Navbar = Navbar(type = NavbarType.FIXEDBOTTOM) {
        height = CssSize(8, UNIT.mm)
        minHeight = CssSize(8, UNIT.mm)
    }
    private val nav = Nav(rightAlign = true)
    private val urlLink = nav.navLink("", icon = "fas fa-history").onClick {
        val model = EventStore.log
        UiManager.add("Log Entries", EventLogTable(model))
    }
    private val userLink = nav.navLink("", icon = "far fa-user")


    init {
        navbar.add(nav)
        nav.add(userLink)
        nav.add(urlLink)
    }

    fun brand(colorCode: String) {
        navbar.setAttribute(name = "color", value = colorCode)
        navbar.nColor = NavbarColor.DARK //IMPROVE changing attributes a runtime possible?
    }

    fun updateUser(user: String) {
        userLink.setAttribute(name = "title", value = user)
        turnGreen()
    }

    fun update(le: LogEntry?) {
        val url = le?.title!!
        urlLink.setAttribute(name = "title", value = url)
    }

    private const val OK = "text-ok"
    private const val DISABLED = "text-disabled"
    private const val WARN = "text-warn"
    fun turnGreen() {
        urlLink.removeCssClass(DISABLED)
        urlLink.removeCssClass(WARN)
        urlLink.addCssClass(OK)
    }

}
