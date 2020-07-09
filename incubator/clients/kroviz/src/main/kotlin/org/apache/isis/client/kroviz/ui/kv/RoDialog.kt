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
package org.apache.isis.client.kroviz.ui.kv

import org.apache.isis.client.kroviz.to.ValueType
import org.apache.isis.client.kroviz.ui.Command
import org.apache.isis.client.kroviz.ui.Displayable
import org.apache.isis.client.kroviz.ui.FormItem
import org.apache.isis.client.kroviz.ui.ImageDialog
import org.apache.isis.client.kroviz.utils.Direction
import org.apache.isis.client.kroviz.utils.IconManager
import org.apache.isis.client.kroviz.utils.Point
import pl.treksoft.kvision.core.CssSize
import pl.treksoft.kvision.core.UNIT
import pl.treksoft.kvision.core.Widget
import pl.treksoft.kvision.form.FormPanel
import pl.treksoft.kvision.html.Button
import pl.treksoft.kvision.html.ButtonStyle
import pl.treksoft.kvision.panel.FlexJustify
import pl.treksoft.kvision.panel.HPanel
import pl.treksoft.kvision.panel.vPanel
import pl.treksoft.kvision.utils.perc

class RoDialog(
        caption: String,
        val items: List<FormItem>,
        val command: Command,
        defaultAction: String = "OK",
        widthPerc: Int = 30,
        heightPerc: Int = 100) :
        Displayable, RoWindow(caption = caption, closeButton = true) {

    private val okButton = Button(
            text = defaultAction,
            icon = IconManager.find(defaultAction),
            style = ButtonStyle.SUCCESS)
            .onClick {
                execute()
            }

    private val cancelButton = Button(
            "Cancel",
            "fas fa-times",
            ButtonStyle.OUTLINEINFO)
            .onClick {
                close()
            }

    @Deprecated("remove once leaflet/svg is fully operational")
    private val scaleUpButton = Button(
            "",
            "fas fa-plus",
            ButtonStyle.OUTLINEINFO)
            .onClick {
                (command as ImageDialog).scale(Direction.UP)
            }

    @Deprecated("remove once leaflet/svg is fully operational")
    private val scaleDownButton = Button(
            "",
            "fas fa-minus",
            ButtonStyle.OUTLINEINFO)
            .onClick {
                (command as ImageDialog).scale(Direction.DOWN)
            }

    var formPanel: FormPanel<String>? = null

    init {
        icon = IconManager.find(caption)
        isDraggable = true
        isResizable = true
        closeButton = true
        contentWidth = CssSize(widthPerc, UNIT.perc)
        contentHeight = CssSize(heightPerc, UNIT.perc)

        vPanel(justify = FlexJustify.SPACEBETWEEN) {
            height = 100.perc
            formPanel = FormPanelFactory(items).panel

            add(formPanel!!, grow = 2)

            val buttonBar = HPanel(
                    spacing = 10,
                    classes = setOf("button-bar"))
            buttonBar.add(okButton)
            buttonBar.add(cancelButton)
            if (items.isNotEmpty() && hasScalableContent()) {
                buttonBar.add(scaleUpButton)
                buttonBar.add(scaleDownButton)
            }
            add(buttonBar)
        }
    }

    private fun execute() {
        command.execute()
        close()
    }

    fun open(at: Point = Point(100, 100)): Widget {
        left = CssSize(at.x, UNIT.px)
        top = CssSize(at.y, UNIT.px)
        UiManager.openDialog(this)
        super.show()
        okButton.focus()
        return this
    }

    override fun close() {
        hide()
        super.remove(this)
        clearParent()
        dispose()
    }

    @Deprecated("remove once leaflet/svg is fully operational")
    private fun hasScalableContent(): Boolean {
        val scalable = items.firstOrNull { it.type == ValueType.IMAGE.type }
        return scalable != null
    }

}
