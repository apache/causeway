package org.ro.ui.kv

import org.ro.core.aggregator.ActionAggregator
import org.ro.to.Link
import org.ro.to.TObject
import org.ro.ui.IconManager
import pl.treksoft.kvision.dropdown.Direction
import pl.treksoft.kvision.dropdown.DropDown
import pl.treksoft.kvision.dropdown.ddLink
import pl.treksoft.kvision.dropdown.separator
import pl.treksoft.kvision.html.ButtonStyle

object MenuFactory {

    fun buildDdFor(tObject: TObject,
                   withText: Boolean = true,
                   style: ButtonStyle = ButtonStyle.LIGHT,
                   direction: Direction = Direction.DROPDOWN): DropDown {
        val type = tObject.domainType
        val dd = DropDown(type, style = style, direction = direction)
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

    fun buildFor(menuEntry: org.ro.to.mb.MenuEntry): DropDown {
        val menu = menuEntry.menu.first()
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
        var classes = setOf("text-normal")
        if (IconManager.isDangerous(label)) {
            classes = setOf("text-danger")
        }
        dd.ddLink(label, icon = icon, classes = classes).onClick { _ ->
            ActionAggregator().invoke(link)
        }
    }

}
