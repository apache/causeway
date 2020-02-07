package org.ro.ui

import org.ro.core.event.EventState
import org.ro.core.event.EventStore
import org.ro.core.event.LogEntry
import org.ro.ui.kv.EventLogTable
import org.ro.ui.kv.UiManager
import pl.treksoft.kvision.core.Background
import pl.treksoft.kvision.core.Col
import pl.treksoft.kvision.core.CssSize
import pl.treksoft.kvision.core.UNIT
import pl.treksoft.kvision.navbar.Nav
import pl.treksoft.kvision.navbar.Navbar
import pl.treksoft.kvision.navbar.NavbarType
import pl.treksoft.kvision.navbar.navLink
import pl.treksoft.kvision.panel.SimplePanel

object RoStatusBar {
    val navbar = Navbar(type = NavbarType.FIXEDBOTTOM) {
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

    fun updateUser(user: String) {
        userLink.setAttribute(name = "title", value = user)
        turnGreen(userLink)
    }

    fun update(le: LogEntry?) {
        val url = le?.title!!
        urlLink.setAttribute(name = "title", value = url)
        when (le.state) {
            EventState.MISSING -> turnRed(nav)
            EventState.ERROR -> turnRed(nav)
            else -> turnGreen(nav)
        }
    }

    private fun turnGreen(panel: SimplePanel) {
        panel.removeCssClass(IconManager.DANGER)
        panel.removeCssClass(IconManager.WARN)
        panel.addCssClass(IconManager.OK)
        nav.background = Background(color = Col.LIGHTGRAY)
    }

    private fun turnRed(panel: SimplePanel) {
        panel.removeCssClass(IconManager.OK)
        panel.removeCssClass(IconManager.WARN)
        panel.addCssClass(IconManager.DANGER)
        nav.background = Background(color = Col.RED)
    }

}
