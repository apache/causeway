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
package org.apache.causeway.client.kroviz.ui.menu

import io.kvision.core.Component
import io.kvision.core.CssSize
import io.kvision.core.UNIT
import io.kvision.dropdown.ContextMenu
import io.kvision.dropdown.separator
import org.apache.causeway.client.kroviz.core.event.ResourceProxy
import org.apache.causeway.client.kroviz.to.Link
import org.apache.causeway.client.kroviz.to.TObject
import org.apache.causeway.client.kroviz.ui.core.Constants
import org.apache.causeway.client.kroviz.utils.IconManager
import org.apache.causeway.client.kroviz.utils.StringUtils
import io.kvision.html.Link as KvisionHtmlLink

object ContextMenuBuilder {

    fun buildForObjectWithSaveAndUndo(tObject: TObject): ContextMenu {
        val cm = buildForObject(tObject)
        amendWithSaveUndo(cm, tObject)
        disableSaveUndo(cm)
        cm.marginTop = CssSize(Constants.spacing, UNIT.px)
        cm.marginBottom = CssSize(Constants.spacing, UNIT.px)
        cm.width = CssSize(100, UNIT.perc)
        return cm
    }

    fun buildForObject(
        tObject: TObject,
        withText: Boolean = true,
//        iconName: String = "Actions",
    )
            : ContextMenu {
        val type = tObject.domainType
        val text = if (withText) "Actions for $type" else ""
//        val icon = IconManager.find(iconName)
        val cm = ContextMenu(
//            text = text,
//            icon = icon,
//            style = ButtonStyle.LINK
        )
        val actions = tObject.getActions()
        actions.forEach {
            val link = buildActionLink(it.id, text)
            val invokeLink = it.getInvokeLink()!!
            link.onClick {
                ResourceProxy().fetch(invokeLink)
            }
            cm.add(link)
        }
        return cm
    }

    fun buildActionLink(
        label: String,
        menuTitle: String,
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
        init: (KvisionHtmlLink.() -> Unit)? = null,
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
    private fun amendWithSaveUndo(
        cm: ContextMenu,
        tObject: TObject,
    ) {
        cm.separator()

        val saveLink = tObject.links.first()
        val saveAction = buildActionLink(
            label = "save",
            menuTitle = tObject.domainType
        )
        saveAction.onClick {
            ResourceProxy().fetch(saveLink)
        }
        cm.add(saveAction)

        val undoLink = Link(href = "")
        val undoAction = buildActionLink(
            label = "undo",
            menuTitle = tObject.domainType
        )
        undoAction.onClick {
            ResourceProxy().fetch(undoLink)
        }
        cm.add(undoAction)
    }

    // disabled when tObject.isClean
    // IMPROVE use tr("Dropdowns (disabled)") to DD.DISABLED.option,
    fun disableSaveUndo(cm: ContextMenu) {
        val menuItems = cm.getChildren()

        val saveItem = menuItems[menuItems.size - 2]
        switchCssClass(saveItem, IconManager.OK, IconManager.DISABLED)

        val undoItem = menuItems[menuItems.size - 1]
        switchCssClass(undoItem, IconManager.OK, IconManager.WARN)
    }

    fun enableSaveUndo(cm: ContextMenu) {
        val menuItems = cm.getChildren()

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
