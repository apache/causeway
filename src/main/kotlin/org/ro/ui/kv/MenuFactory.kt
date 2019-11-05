package org.ro.ui.kv

import org.ro.core.aggregator.ActionAggregator
import org.ro.to.TObject
import org.ro.ui.IconManager
import pl.treksoft.kvision.dropdown.ContextMenu
import pl.treksoft.kvision.dropdown.DropDown
import pl.treksoft.kvision.dropdown.cmLink
import pl.treksoft.kvision.dropdown.header
import pl.treksoft.kvision.html.Link
import pl.treksoft.kvision.i18n.tr
import pl.treksoft.kvision.utils.ESC_KEY

object MenuFactory {

    fun buildMenuEntry(title: String, iconName: String? = null): DropDown {
        val label = tr(title)
        val icon = iconName ?: IconManager.find(title)
        return DropDown(label, icon = icon, forNavbar = true)
    }

    fun buildMenuAction(action: String, iconName: String? = null): Link {
        val label = tr(action)
        val icon = iconName ?: IconManager.find(action)
        return Link(label, icon = icon, classes = setOf("dropdown-toggle"))
    }

    fun buildFor(tObject: TObject): ContextMenu {
        val type = tObject.domainType
        val actions = tObject.getActions()
        return ContextMenu {
            header(tr("Actions for $type"))
            actions.forEach {
                val title = it.id
                val iconName = IconManager.find(title)
                val link = it.getInvokeLink()!!
                cmLink(tr(title), icon = iconName) {
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
    }

    // tr("Separator") to DD.SEPARATOR.option,
    fun buildDdFor(tObject: TObject): DropDown {
        val type = tObject.domainType
        val actions = tObject.getActions()
        val label = "Actions for $type"
        val iconName = IconManager.find(label)
        val dd = buildMenuEntry(label, iconName = iconName)
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

}
