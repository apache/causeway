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

import kotlinx.browser.document
import kotlinx.dom.removeClass
import org.apache.isis.client.kroviz.core.event.ResourceSpecification
import org.apache.isis.client.kroviz.core.model.Exposer
import org.apache.isis.client.kroviz.to.TObject
import org.apache.isis.client.kroviz.ui.core.MenuFactory.buildForTitle
import org.apache.isis.client.kroviz.utils.IconManager
import org.apache.isis.client.kroviz.utils.StringUtils
import io.kvision.core.CssSize
import io.kvision.core.UNIT
import io.kvision.core.Widget
import io.kvision.dropdown.DropDown
import io.kvision.html.Button
import io.kvision.html.ButtonStyle
import io.kvision.panel.SimplePanel
import io.kvision.panel.VPanel

class RoIconBar : SimplePanel() {

    val panel = VPanel()
    private val icons = mutableListOf<SimplePanel>()

    init {
        panel.addCssClass("icon-bar")
        panel.title = "Drop objects, factories, or actions here"
        add(createDeleteIcon())
        panel.setDropTargetData(Constants.stdMimeType) { id ->
            when {
                StringUtils.isUrl(id!!) ->
                    add(createObjectIcon(id)!!)
                id.contains(Constants.actionSeparator) ->
                    add(createActionIcon(id))
                else ->
                    add(createFactoryIcon(id))
            }
        }
        hide()
    }

    fun add(icon: SimplePanel) {
        icons.add(icon)
        panel.add(icon)
    }

    fun toggle() {
        if (panel.width?.first == 0) show() else hide()
    }

    override fun hide(): Widget {
        panel.width = CssSize(0, UNIT.px)
        panel.removeAll()
        return super.hide()
    }

    override fun show(): Widget {
        panel.width = CssSize(40, UNIT.px)
        icons.forEach { panel.add(it) }
        return super.show()
    }

    private fun createDeleteIcon(): Button {
        val del = Button(
                text = "",
                icon = IconManager.find("Delete"),
                style = ButtonStyle.LIGHT).apply {
            padding = CssSize(-16, UNIT.px)
            margin = CssSize(0, UNIT.px)
            title = "Drop icon here in order to remove it"
        }
        del.setDropTargetData(Constants.stdMimeType) {
            icons.forEach { ii ->
                if (ii.id == it) {
                    icons.remove(ii)
                    panel.remove(ii)
                }
            }
        }
        return del
    }

    private fun createObjectIcon(url: String): DropDown? {
        val reSpec = ResourceSpecification(url)
        val logEntry = SessionManager.getEventStore().findBy(reSpec)!!
        return when (val obj = logEntry.obj) {
            (obj == null) -> null
            is TObject -> {
                val exp = Exposer(obj)
                val ed = exp.dynamise()
                val hasIconName = ed.hasOwnProperty("iconName") as Boolean
                val iconName = if (hasIconName) (ed["iconName"] as String) else ""

                val icon = MenuFactory.buildForObject(
                        tObject = obj,
                        iconName = iconName,
                        withText = false)
                var title = StringUtils.extractTitle(logEntry.title)
                title += "\n${obj.title}"
                initIcon(icon, url, title, "icon-bar-object", icon.buttonId()!!)
                icon
            }
            else -> null
        }
    }

    private fun createActionIcon(id: String): SimplePanel {
        val titles = id.split(Constants.actionSeparator)
        val menuTitle = titles[0]
        val actionTitle = titles[1]
        val icon = MenuFactory.buildForAction(menuTitle, actionTitle)!!
        return initIcon(icon, id, id, "icon-bar-action", icon.id!!)
    }

    private fun createFactoryIcon(id: String): SimplePanel {
        val icon = buildForTitle(id)!!
        return initIcon(icon, id, id, "icon-bar-factory", icon.buttonId()!!)
    }

    private fun initIcon(icon: SimplePanel,
                         id: String,
                         title: String,
                         cssClass: String,
                         btnId: String)
            : SimplePanel {
        icon.setDragDropData(Constants.stdMimeType, id)
        icon.id = id
        icon.title = title
        icon.addCssClass(cssClass)
        addAfterInsertHook {
            val btn = document.getElementById(btnId)!!
            btn.removeClass("dropdown-toggle")
        }
        return icon
    }

}
