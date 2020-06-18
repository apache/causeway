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

    fun buildFor(tObject: TObject,
                 withText: Boolean = true,
                 iconName: String = "Actions",
                 style: ButtonStyle = ButtonStyle.LIGHT)
            : DropDown {
        val type = tObject.domainType
        val text = if (withText) "Actions for $type" else ""
        val icon = IconManager.find(iconName)
        val dd = DropDown(text = text, icon = icon, style = style)
        val actions = tObject.getActions()
        actions.forEach {
            val title = it.id
            val link = it.getInvokeLink()!!
            val action = buildAction(title, link, text)
            dd.add(action)
        }
        return dd
    }

    fun buildFor(menu: Menu,
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
                val action = buildAction(sa.id!!, sa.link!!, menuTitle)
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
            buildFor(
                    menu = menu,
                    withText = false)
    }

    private fun findMenuByTitle(title: String): Menu? {
        val menuBars = EventStore.findMenuBars()!!.obj as Menubars
        var menu = findMenu(menuBars.primary, title)
        if (menu == null) menu = findMenu(menuBars.secondary, title)
        if (menu == null) menu = findMenu(menuBars.tertiary, title)
        return menu
    }

    private fun findMenu(menuEntry: MenuEntry, title: String): Menu? {
        return menuEntry.menu.firstOrNull { it.named == title }
    }

    fun buildForAction(menuTitle: String, actionTitle: String): KvisionHtmlLink? {
        val menu = findMenuByTitle(menuTitle)!!
        menu.section.forEachIndexed { _, section ->
            section.serviceAction.forEach { sa ->
                if (actionTitle == sa.named) {
                    val action = buildAction(sa.id!!, sa.link!!, menuTitle)
                    action.label = ""
                    return action
                }
            }
        }
        return null
    }

    private fun buildAction(label: String, link: Link, menuTitle: String): KvisionHtmlLink {
        val icon = IconManager.find(label)
        val classes = IconManager.findStyleFor(label)
        val actionTitle = Utils.deCamel(label)
        val action: KvisionHtmlLink = ddLink(actionTitle, icon = icon, classes = classes)
        action.onClick {
            ActionDispatcher().invoke(link)
        }
        val title = "$menuTitle${Constants.actionSeparator}$actionTitle"
        action.setDragDropData(Constants.format, title)
        action.id = title
        return action
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
    fun amendWithSaveUndo(dd: DropDown, tObject: TObject) {
        dd.separator()

        val saveLink = tObject.links.first()
        val saveAction = buildAction(
                label = "save",
                link = saveLink,
                menuTitle = tObject.domainType)
        dd.add(saveAction)

        val undoLink = Link(href = "")
        val undoAction = buildAction(
                label = "undo",
                link = undoLink,
                menuTitle = tObject.domainType)
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
