package org.ro.ui.kv

import org.ro.core.aggregator.ActionAggregator
import org.ro.to.TObject
import org.ro.ui.IconManager
import pl.treksoft.kvision.core.onEvent
import pl.treksoft.kvision.dropdown.Direction
import pl.treksoft.kvision.dropdown.DropDown
import pl.treksoft.kvision.dropdown.ddLink
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
            val iconName = IconManager.find(title)
            val link = it.getInvokeLink()!!
            var styles = setOf("text-normal")
            if (IconManager.isDangerous(title)) {
                styles = setOf("text-danger")
            }
            val ddl = dd.ddLink(title, icon = iconName, classes = styles)
            ddl.onEvent {
                click = { e ->
                    e.stopPropagation()
                    ActionAggregator().invoke(link)
                }
            }
            dd.add(ddl)
        }
        return dd
    }

}

