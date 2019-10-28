package org.ro.ui.kv

import org.ro.core.aggregator.ActionAggregator
import org.ro.to.TObject
import org.ro.ui.IconManager
import pl.treksoft.kvision.dropdown.ContextMenu
import pl.treksoft.kvision.dropdown.DropDown
import pl.treksoft.kvision.dropdown.Header.Companion.header
import pl.treksoft.kvision.html.Link
import pl.treksoft.kvision.html.Link.Companion.link
import pl.treksoft.kvision.i18n.I18n
import pl.treksoft.kvision.utils.ESC_KEY

object MenuFactory {

    fun buildDdFor(tObject: TObject): DropDown {
        val type = tObject.domainType
        val actions = tObject.getActions()
        val label =  "Actions for $type"
        val iconName = "fa-ellipsis-v"
        val dd = buildMenuEntry(label, iconName = iconName, forNavbar = false)
        actions.forEach {
            val menuLink = buildMenuAction(it.id)
            dd.add(menuLink)
            val l = it.getInvokeLink()!!
            menuLink.onClick {
                ActionAggregator().invoke(l)
            }
        }
        return dd
    }

     fun buildMenuEntry(title: String, iconName: String? = null, forNavbar:Boolean = true): DropDown {
        val label = I18n.tr(title)
        val icon = iconName ?: IconManager.find(title)
        return DropDown(label, icon = icon, forNavbar = forNavbar)
    }

     fun buildMenuAction(action: String, iconName: String? = null): Link {
        val label = I18n.tr(action)
        val icon = iconName ?: IconManager.find(action)
        return Link(label, icon = icon)
    }

    fun buildFor(tObject: TObject): ContextMenu {
        val type = tObject.domainType
        val actions = tObject.getActions()
        val contextMenu = ContextMenu {
            header(I18n.tr("Actions for $type"))
            actions.forEach {
                val title = it.id
                val iconName = IconManager.find(title)
                val link = it.getInvokeLink()!!
                link(I18n.tr(title), icon = iconName) {
                    setEventListener {
                        click = { e ->
                            e.stopPropagation()
                            ActionAggregator().invoke(link)
                            this@ContextMenu.hide()
                        }
                        keydown = { k ->
                            k.stopPropagation()
                            if (k.keyCode == ESC_KEY) {
                                this@ContextMenu.hide()
                            }
                        }
                    }
                }
            }
        }
        return contextMenu
    }

}
