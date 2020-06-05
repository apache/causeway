package org.apache.isis.client.kroviz.ui.kv

import org.apache.isis.client.kroviz.core.event.EventState
import org.apache.isis.client.kroviz.core.event.LogEntry
import org.apache.isis.client.kroviz.core.model.DiagramDM
import org.apache.isis.client.kroviz.ui.ClassDiagram
import org.apache.isis.client.kroviz.ui.IconManager
import org.apache.isis.client.kroviz.ui.ImageDialog
import org.apache.isis.client.kroviz.ui.NotificationDialog
import pl.treksoft.kvision.core.*
import pl.treksoft.kvision.html.Button
import pl.treksoft.kvision.html.ButtonStyle
import pl.treksoft.kvision.navbar.Nav
import pl.treksoft.kvision.navbar.Navbar
import pl.treksoft.kvision.navbar.NavbarType
import pl.treksoft.kvision.panel.SimplePanel

object RoStatusBar {
    val navbar = Navbar(type = NavbarType.FIXEDBOTTOM) {
        height = CssSize(8, UNIT.mm)
        minHeight = CssSize(8, UNIT.mm)
        width = CssSize(100, UNIT.perc)
    }
    private val nav = Nav(rightAlign = true)
    private val userBtn: Button = buildButton("", "Me", ButtonStyle.OUTLINEWARNING)
    private val umlDiagram = buildButton("", "Diagram", ButtonStyle.OUTLINEWARNING)
    private val lastError = buildButton("OK", "OK", ButtonStyle.OUTLINESUCCESS)
    private val alert = buildButton("", "Notification", ButtonStyle.OUTLINEINFO)

    private fun buildButton(text: String, iconName: String, style: ButtonStyle): Button {
        return Button(
                text = text,
                icon = IconManager.find(iconName),
                style = style).apply {
            padding = CssSize(-16, UNIT.px)
            margin = CssSize(0, UNIT.px)
        }
    }

    init {
        navbar.add(nav)
        nav.add(lastError)
        nav.add(umlDiagram)
        nav.add(userBtn)
        nav.add(alert)
    }

    fun updateDiagram(dd: DiagramDM) {
        umlDiagram.style = ButtonStyle.OUTLINESUCCESS
        umlDiagram.onClick {
            val title = dd.title
            val code = ClassDiagram.buildDiagramCode(dd)
            ImageDialog(title, code).open()
        }
    }

    fun updateUser(user: String) {
        userBtn.setAttribute(name = "title", value = user)
        userBtn.style = ButtonStyle.OUTLINESUCCESS
    }

    private fun notify(text: String) {
        alert.setAttribute(name = "title", value = text)
        alert.style = ButtonStyle.OUTLINEDANGER
        alert.onClick {
            NotificationDialog(text).open()
        }
    }

    fun acknowledge() {
        alert.setAttribute(name = "title", value = "no new notifications")
        alert.style = ButtonStyle.OUTLINELIGHT
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
        notify(text)
    }

}
