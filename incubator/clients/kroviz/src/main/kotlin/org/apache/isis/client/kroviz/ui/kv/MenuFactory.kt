package org.apache.isis.client.kroviz.ui.kv

import org.apache.isis.client.kroviz.core.aggregator.ActionDispatcher
import org.apache.isis.client.kroviz.core.event.EventStore
import org.apache.isis.client.kroviz.to.Link
import org.apache.isis.client.kroviz.to.Member
import org.apache.isis.client.kroviz.to.TObject
import org.apache.isis.client.kroviz.to.mb.Menu
import org.apache.isis.client.kroviz.to.mb.MenuEntry
import org.apache.isis.client.kroviz.to.mb.Menubars
import org.apache.isis.client.kroviz.utils.IconManager
import org.apache.isis.client.kroviz.utils.Utils
import pl.treksoft.kvision.core.Component
import pl.treksoft.kvision.dropdown.DropDown
import pl.treksoft.kvision.dropdown.separator
import pl.treksoft.kvision.html.ButtonStyle
import pl.treksoft.kvision.utils.set
import pl.treksoft.kvision.html.Link as KvisionHtmlLink

object MenuFactory {

    fun buildForObject(tObject: TObject,
                       withText: Boolean = true,
                       iconName: String = "Actions")
            : DropDown {
        val type = tObject.domainType
        val text = if (withText) "Actions for $type" else ""
        val icon = IconManager.find(iconName)
        val dd = DropDown(
                text = text,
                icon = icon,
                style = ButtonStyle.LINK)
        val actions = tObject.getActions()
        actions.forEach {
            val link = buildActionLink(it.id, text)
            val invokeLink = it.getInvokeLink()!!
            link.onClick {
                ActionDispatcher().invoke(invokeLink)
            }
            dd.add(link)
        }
        return dd
    }

    fun buildForMenu(menu: Menu,
                     style: ButtonStyle = ButtonStyle.LIGHT,
                     withText: Boolean = true,
                     classes: Set<String> = setOf())
            : DropDown {
        val menuTitle = menu.named
        val dd = DropDown(
                text = if (withText) menuTitle else "",
                icon = IconManager.find(menuTitle),
                style = style,
                classes = classes,
                forNavbar = false)
        //dd.setDragDropData(Constants.format, menuTitle)
        // action.setDragDropData gets always overridden by dd.setDragDropData
        menu.section.forEachIndexed { index, section ->
            section.serviceAction.forEach { sa ->
                val action = buildActionLink(sa.id!!, menuTitle)
                action.onClick {
                    ActionDispatcher().invoke(sa.link!!)
                }
                action.setDragDropData(Constants.format, action.id!!)
                dd.add(action)
            }
            if (index < menu.section.size - 1) {
                dd.separator()
            }
        }
        return dd
    }

    fun buildForTitle(title: String): DropDown? {
        val menu = findMenuByTitle(title)
        return if (menu == null) null else
            buildForMenu(
                    menu = menu,
                    withText = false)
    }

    private fun findMenuByTitle(menuTitle: String): Menu? {
        val menuBars = EventStore.findMenuBars()!!.obj as Menubars
        var menu = findMenu(menuBars.primary, menuTitle)
        if (menu == null) menu = findMenu(menuBars.secondary, menuTitle)
        if (menu == null) menu = findMenu(menuBars.tertiary, menuTitle)
        return menu
    }

    private fun findMenu(menuEntry: MenuEntry, menuTitle: String): Menu? {
        return menuEntry.menu.firstOrNull { it.named == menuTitle }
    }

    fun buildForAction(
            menuTitle: String,
            actionTitle: String): KvisionHtmlLink? {
        console.log("[MF.buildForAction] $menuTitle / $actionTitle")
        val menu = findMenuByTitle(menuTitle)!!
        menu.section.forEachIndexed { _, section ->
            section.serviceAction.forEach { sa ->
                val saTitle = Utils.deCamel(sa.id!!)
                console.log(saTitle)
                if (saTitle == actionTitle) {
                    val action = buildActionLink(sa.id, menuTitle)
                    action.label = ""
                    action.onClick {
                        ActionDispatcher().invoke(sa.link!!)
                    }
                    return action
                }
            }
        }
        return null
    }

    private fun buildActionLink(
            label: String,
            menuTitle: String): KvisionHtmlLink {
        val actionTitle = Utils.deCamel(label)
        val actionLink: KvisionHtmlLink = ddLink(
                label = actionTitle,
                icon = IconManager.find(label),
                classes = IconManager.findStyleFor(label))
        val id = "$menuTitle${Constants.actionSeparator}$actionTitle"
        actionLink.setDragDropData(Constants.format, id)
        actionLink.id = id
        return actionLink
    }

    private fun ddLink(
            label: String,
            icon: String? = null,
            classes: Set<String>? = null,
            init: (KvisionHtmlLink.() -> Unit)? = null
    ): KvisionHtmlLink {
        return KvisionHtmlLink(
                label = label,
                url = null,
                icon = icon,
                image = null,
                separator = null,
                labelFirst = true,
                classes = (classes ?: null.set) + "dropdown-item").apply {
            init?.invoke(this)
        }
    }

    // initially added items will be enabled
    fun amendWithSaveUndo(
            dd: DropDown,
            tObject: TObject) {
        dd.separator()

        val saveLink = tObject.links.first()
        val saveAction = buildActionLink(
                label = "save",
                menuTitle = tObject.domainType)
        saveAction.onClick {
            ActionDispatcher().invoke(saveLink)
        }
        dd.add(saveAction)

        val undoLink = Link(href = "")
        val undoAction = buildActionLink(
                label = "undo",
                menuTitle = tObject.domainType)
        undoAction.onClick {
            ActionDispatcher().invoke(undoLink)
        }
        dd.add(undoAction)
    }

    // disabled when tObject.isClean
    // IMPROVE use tr("Dropdowns (disabled)") to DD.DISABLED.option,
    fun disableSaveUndo(dd: DropDown) {
        val menuItems = dd.getChildren()

        val saveItem = menuItems[menuItems.size - 2]
        switchCssClass(saveItem, IconManager.OK, IconManager.DISABLED)

        val undoItem = menuItems[menuItems.size - 1]
        switchCssClass(undoItem, IconManager.OK, IconManager.WARN)
    }

    fun enableSaveUndo(dd: DropDown) {
        val menuItems = dd.getChildren()

        val saveItem = menuItems[menuItems.size - 2]
        switchCssClass(saveItem, IconManager.DISABLED, IconManager.OK)

        val undoItem = menuItems[menuItems.size - 1]
        switchCssClass(undoItem, IconManager.DISABLED, IconManager.WARN)
    }

    private fun switchCssClass(menuItem: Component, from: String, to: String) {
        menuItem.removeCssClass(from)
        menuItem.addCssClass(to)
    }

    private fun Member.getInvokeLink(): Link? {
        return links.firstOrNull { it.rel.indexOf(id) > 0 }
    }

}
