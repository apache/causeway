package org.ro.ui.kv

import org.ro.core.aggregator.ActionDispatcher
import org.ro.to.Link
import org.ro.to.Member
import org.ro.to.TObject
import org.ro.to.mb.Menu
import org.ro.ui.IconManager
import pl.treksoft.kvision.core.Component
import pl.treksoft.kvision.dropdown.Direction
import pl.treksoft.kvision.dropdown.DropDown
import pl.treksoft.kvision.dropdown.ddLink
import pl.treksoft.kvision.dropdown.separator
import pl.treksoft.kvision.html.ButtonStyle

object MenuFactory {

    fun buildFor(tObject: TObject,
                 withText: Boolean = true,
                 style: ButtonStyle = ButtonStyle.LIGHT): DropDown {
        val type = tObject.domainType
        val dd = DropDown(
                type,
                style = style,
                direction = Direction.DROPDOWN
        )
        if (withText) {
            dd.text = "Actions for $type"
        } else {
            dd.text = ""
        }
        dd.icon = IconManager.find("Actions")
        val actions = tObject.getActions()
        actions.forEach {
            val title = it.id
            val link = it.getInvokeLink()!!
            action(dd, title, link)
        }
        return dd
    }

    fun buildFor(menu: Menu): DropDown {
        val title = menu.named
        val dd = DropDown(text = title, style = ButtonStyle.LIGHT, forNavbar = false)
        dd.icon = IconManager.find(title)
        menu.section.forEachIndexed { index, section ->
            section.serviceAction.forEach { sa ->
                action(dd, sa.id!!, sa.link!!)
            }
            if (index < menu.section.size - 1) {
                dd.separator()
            }
        }
        return dd
    }
    private fun action(dd: DropDown, label: String, link: Link) {
        val icon = IconManager.find(label)
        val classes = IconManager.findStyleFor(label)
        dd.ddLink(label, icon = icon, classes = classes).onClick {
            ActionDispatcher().invoke(link)
        }
    }

    // initially added items will be enabled
    fun amendWithSaveUndo(dd: DropDown, tObject: TObject) {
        dd.separator()

        val saveLink = tObject.links.first()
        action(dd, "save", saveLink)

        val undoLink = Link()
        action(dd, "undo", undoLink)
    }

    // disabled when tObject.isClean
    // IMPROVE use tr("Dropdowns (disabled)") to DD.DISABLED.option,
    private const val OK = "text-ok"
    private const val DISABLED = "text-disabled"
    private const val WARN = "text-warn"
    fun disableSaveUndo(dd: DropDown) {
        val menuItems = dd.getChildren()

        val saveItem = menuItems[menuItems.size - 2]
        switchCssClass(saveItem, OK, DISABLED)

        val undoItem = menuItems[menuItems.size - 1]
        switchCssClass(undoItem, OK, WARN)
    }

    fun enableSaveUndo(dd: DropDown) {
        val menuItems = dd.getChildren()

        val saveItem = menuItems[menuItems.size - 2]
        switchCssClass(saveItem, DISABLED, OK)

        val undoItem = menuItems[menuItems.size - 1]
        switchCssClass(undoItem, DISABLED, WARN)
    }

    private fun switchCssClass(menuItem: Component, from:String, to:String) {
        menuItem.removeCssClass(from)
        menuItem.addCssClass(to)
    }

    fun Member.getInvokeLink(): Link? {
        for (l in links) {
            if (l.rel.indexOf(id) > 0) {
                return l
            }
        }
        return null
    }

}
