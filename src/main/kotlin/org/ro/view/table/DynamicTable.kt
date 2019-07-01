package org.ro.view.table

import org.ro.core.event.LogEntry
import pl.treksoft.kvision.dropdown.DropDown
import pl.treksoft.kvision.html.Link
import pl.treksoft.kvision.i18n.I18n
import pl.treksoft.kvision.panel.SimplePanel

/**
 * access attributes from dynamic (JS) objects with varying
 * - numbers of attributes
 * - attribute types (can only be determined at runtime) and
 * - accessor names are not fixed
 */
class DynamicTable(private val tableSpec:List<Any>) : SimplePanel() {

    //TODO see EventLogTable
    //button(tr("Link button"), style = ButtonStyle.LINK) { width = 200.px }

    fun build(logEntry: LogEntry): DropDown {
        val menu = buildMenuEntry("Action(s) ...", iconName = "fa-ellipsis-h")

        val link = Link(I18n.tr("Details"), icon = "fa-server").onClick {
            console.log("[ActionMenu.build] $logEntry")
        }
        menu.add(link)

        return menu
    }

    private fun buildMenuEntry(title: String, iconName: String? = null): DropDown {
        val label = I18n.tr(title)
        val icon = iconName ?: "fa-bolt"
        return DropDown(label, icon = icon, forNavbar = true)
    }

    private fun buildMenuAction(action: String, iconName: String? = null): Link {
        val label = I18n.tr(action)
        val icon = iconName ?: "fa-bolt"
        return Link(label, icon = icon)
    }



}
