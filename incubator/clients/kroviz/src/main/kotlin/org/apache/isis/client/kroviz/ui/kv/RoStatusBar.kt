package org.apache.isis.client.kroviz.ui.kv

import org.apache.isis.client.kroviz.core.event.EventState
import org.apache.isis.client.kroviz.core.event.LogEntry
import org.apache.isis.client.kroviz.core.model.DiagramDM
import org.apache.isis.client.kroviz.ui.ClassDiagram
import org.apache.isis.client.kroviz.ui.IconManager
import org.apache.isis.client.kroviz.ui.ImageAlert
import pl.treksoft.kvision.core.*
import pl.treksoft.kvision.html.Button
import pl.treksoft.kvision.html.ButtonStyle
import pl.treksoft.kvision.navbar.Nav
import pl.treksoft.kvision.navbar.Navbar
import pl.treksoft.kvision.navbar.NavbarType
import pl.treksoft.kvision.navbar.navLink
import pl.treksoft.kvision.panel.SimplePanel

object RoStatusBar {
    val navbar = Navbar(type = NavbarType.FIXEDBOTTOM) {
        height = CssSize(8, UNIT.mm)
        minHeight = CssSize(8, UNIT.mm)
        width = CssSize(100, UNIT.perc)
    }
    private val nav = Nav(rightAlign = true)
    private val userLink = nav.navLink("", icon = "far fa-user")
    private val umlDiagram: Button = Button(
            text = "",
            icon = IconManager.find("Diagram"),
            style = ButtonStyle.OUTLINEWARNING).apply {
        padding = CssSize(-16, UNIT.px)
        margin = CssSize(0, UNIT.px)
    }
    private val lastError: Button = Button(
            text = "OK",
            icon = IconManager.find("OK"),
            style = ButtonStyle.OUTLINESUCCESS).apply {
        padding = CssSize(-16, UNIT.px)
        margin = CssSize(0, UNIT.px)
    }

    init {
        navbar.add(nav)
        nav.add(lastError)
        nav.add(umlDiagram)
        nav.add(userLink)
    }

    fun updateDiagram(dd: DiagramDM) {
        umlDiagram.style = ButtonStyle.OUTLINESUCCESS
        umlDiagram.onClick {
            val title = dd.title
            val code = ClassDiagram.buildDiagramCode(dd)
            ImageAlert(title, code).open()
        }
    }

    fun updateUser(user: String) {
        userLink.setAttribute(name = "title", value = user)
        turnGreen(userLink)
    }

    fun update(le: LogEntry?) {
        when (le?.state) {
            EventState.ERROR -> turnRed(le)
            EventState.MISSING -> turnRed(le)
            else -> turnGreen(nav)
        }
    }

    private fun turnGreen(panel: SimplePanel) {
        panel.removeCssClass(IconManager.DANGER)
        panel.removeCssClass(IconManager.WARN)
        panel.addCssClass(IconManager.OK)
        navbar.background = Background(color = Color.name(Col.LIGHTGRAY))
    }

    private fun turnRed(logEntry: LogEntry) {
        var text = logEntry.url
        if (text.length > 50) text = text.substring(0, 49)
        lastError.text = text
        lastError.style = ButtonStyle.OUTLINEDANGER
        lastError.icon = IconManager.find("Error")
    }

}
