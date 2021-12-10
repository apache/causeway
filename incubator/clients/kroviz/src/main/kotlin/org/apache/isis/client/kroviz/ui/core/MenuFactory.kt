/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.client.kroviz.ui.core

import io.kvision.core.Component
import io.kvision.dropdown.DropDown
import io.kvision.dropdown.separator
import io.kvision.html.ButtonStyle
import org.apache.isis.client.kroviz.core.event.ResourceProxy
import org.apache.isis.client.kroviz.to.Link
import org.apache.isis.client.kroviz.to.TObject
import org.apache.isis.client.kroviz.to.mb.Menu
import org.apache.isis.client.kroviz.to.mb.MenuEntry
import org.apache.isis.client.kroviz.to.mb.Menubars
import org.apache.isis.client.kroviz.utils.IconManager
import org.apache.isis.client.kroviz.utils.StringUtils
import io.kvision.html.Link as KvisionHtmlLink

object MenuFactory {

    fun buildForObject(
        tObject: TObject,
        withText: Boolean = true,
        iconName: String = "Actions"
    )
            : DropDown {
        val type = tObject.domainType
        val text = if (withText) "Actions for $type" else ""
        val icon = IconManager.find(iconName)
        val dd = DropDown(
            text = text,
            icon = icon,
            style = ButtonStyle.LINK
        )
        val actions = tObject.getActions()
        actions.forEach {
            val link = buildActionLink(it.id, text)
            val invokeLink = it.getInvokeLink()!!
            link.onClick {
                ResourceProxy().fetch(invokeLink)
            }
            dd.add(link)
        }
        return dd
    }

    fun buildForMenu(
        menu: Menu,
        style: ButtonStyle = ButtonStyle.LIGHT,
        withText: Boolean = true,
        className: String? = null
    )
            : DropDown {
        val menuTitle = menu.named
        val dd = DropDown(
            text = if (withText) menuTitle else "",
            icon = IconManager.find(menuTitle),
            style = style,
            className = className,
            forNavbar = false
        )
        //dd.setDragDropData(Constants.stdMimeType, menuTitle)
        // action.setDragDropData gets always overridden by dd.setDragDropData
        menu.section.forEachIndexed { index, section ->
            section.serviceAction.forEach { sa ->
                val action = buildActionLink(sa.id!!, menuTitle)
                action.onClick {
                    ResourceProxy().fetch(sa.link!!)
                }
                action.setDragDropData(Constants.stdMimeType, action.id!!)
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
                withText = false
            )
    }

    private fun findMenuByTitle(menuTitle: String): Menu? {
        val menuBars = SessionManager.getEventStore().findMenuBars()!!.obj as Menubars
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
        actionTitle: String
    ): KvisionHtmlLink? {
        val menu = findMenuByTitle(menuTitle)!!
        menu.section.forEachIndexed { _, section ->
            section.serviceAction.forEach { sa ->
                val saTitle = StringUtils.deCamel(sa.id!!)
                if (saTitle == actionTitle) {
                    val action = buildActionLink(sa.id, menuTitle)
                    action.label = ""
                    action.onClick {
                        ResourceProxy().fetch(sa.link!!)
                    }
                    return action
                }
            }
        }
        return null
    }

    fun buildActionLink(
        label: String,
        menuTitle: String
    ): KvisionHtmlLink {
        val actionTitle = StringUtils.deCamel(label)
        val actionLink: KvisionHtmlLink = ddLink(
            label = actionTitle,
            icon = IconManager.find(label),
            className = IconManager.findStyleFor(label)
        )
        val id = "$menuTitle${Constants.actionSeparator}$actionTitle"
        actionLink.setDragDropData(Constants.stdMimeType, id)
        actionLink.id = id
        return actionLink
    }

    private fun ddLink(
        label: String,
        icon: String? = null,
        className: String? = null,
        init: (KvisionHtmlLink.() -> Unit)? = null
    ): KvisionHtmlLink {
        val link = KvisionHtmlLink(
            label = label,
            url = null,
            icon = icon,
            image = null,
            separator = null,
            labelFirst = true,
            className = className
        )
        link.addCssClass("dropdown-item")
        return link.apply {
            init?.invoke(this)
        }
    }

    // initially added items will be enabled
    fun amendWithSaveUndo(
        dd: DropDown,
        tObject: TObject
    ) {
        dd.separator()

        val saveLink = tObject.links.first()
        val saveAction = buildActionLink(
            label = "save",
            menuTitle = tObject.domainType
        )
        saveAction.onClick {
            ResourceProxy().fetch(saveLink)
        }
        dd.add(saveAction)

        val undoLink = Link(href = "")
        val undoAction = buildActionLink(
            label = "undo",
            menuTitle = tObject.domainType
        )
        undoAction.onClick {
            ResourceProxy().fetch(undoLink)
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

}
