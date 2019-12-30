package org.ro.ui.kv

import org.ro.core.aggregator.ActionAggregator
import org.ro.to.TObject
import org.ro.ui.IconManager
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.events.MouseEventInit
import pl.treksoft.kvision.core.CssSize
import pl.treksoft.kvision.core.UNIT
import pl.treksoft.kvision.dropdown.*
import pl.treksoft.kvision.html.ButtonStyle
import pl.treksoft.kvision.i18n.tr

object MenuFactory {

    fun buildContextMenu(mouseEvent: MouseEvent, tObject: TObject): ContextMenu {
        console.log("[MenuFactory.buildContextMenu]")
        console.log(mouseEvent)
        val menu = buildFor(tObject)
        //FIXME x/y coordinates are sometimes off screen
        val x = mouseEvent.pageX
        val y = mouseEvent.pageY
        console.log(x)
        console.log(y)
        val mei = MouseEventInit(
                screenY = y.toInt(),
                clientY = y.toInt())
        val positionedEvent = MouseEvent("", mei)
        menu.positionMenu(positionedEvent)
        //      menu.left = CssSize(0, UNIT.px)
        menu.top = CssSize(y, UNIT.px)
        //       menu.position = Position()
        console.log(menu)
        return menu
    }

    private fun buildFor(tObject: TObject): ContextMenu {
        val type = tObject.domainType
        val actions = tObject.getActions()
        return ContextMenu {
            header(tr("Actions for $type"))
            actions.forEach {
                val title = it.id
                val iconName = IconManager.find(title)
                val link = it.getInvokeLink()!!
                var styles = setOf("text-normal")
                if (IconManager.isDangerous(title)) {
                    styles = setOf("text-danger")
                }
                cmLink(tr(title), icon = iconName, classes = styles) {
                    setEventListener {
                        click = { e ->
                            e.stopPropagation()
                            ActionAggregator().invoke(link)
                            this@ContextMenu.hide()
                        }
                    }
                }
            }
        }
    }

    fun buildDdFor(tObject: TObject): DropDown {
        val type = tObject.domainType
        val dd = DropDown(type, style = ButtonStyle.LIGHT)
        dd.text = "Actions for $type"
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
            ddl.setEventListener {
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

