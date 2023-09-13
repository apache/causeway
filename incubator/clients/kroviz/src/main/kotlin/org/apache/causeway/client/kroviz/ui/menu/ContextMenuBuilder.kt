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

import io.kvision.core.CssSize
import io.kvision.core.UNIT
import io.kvision.dropdown.ContextMenu
import io.kvision.dropdown.separator
import org.apache.causeway.client.kroviz.core.event.ResourceProxy
import org.apache.causeway.client.kroviz.to.Link
import org.apache.causeway.client.kroviz.to.TObject
import org.apache.causeway.client.kroviz.ui.core.Constants
import org.apache.causeway.client.kroviz.utils.IconManager

class ContextMenuBuilder : MenuBuilder() {

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
    )
            : ContextMenu {
        val type = tObject.domainType
        val text = if (withText) "Actions for $type" else ""
        val cm = ContextMenu()
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
    private fun disableSaveUndo(cm: ContextMenu) {
        switchSaveUndo(cm, IconManager.OK, IconManager.DISABLED)
    }

    fun enableSaveUndo(cm: ContextMenu) {
        switchSaveUndo(cm, IconManager.DISABLED, IconManager.OK)
    }

    fun switchSaveUndo(cm: ContextMenu, icon1: String, icon2: String) {
        val menuItems = cm.getChildren()

        val saveItem = menuItems[menuItems.size - 2]
        switchCssClass(saveItem, icon1, icon2)

        val undoItem = menuItems[menuItems.size - 1]
        switchCssClass(undoItem, icon1, IconManager.WARN)
    }

}
