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
import org.apache.isis.client.kroviz.ui.FormItem
import org.apache.isis.client.kroviz.utils.DateHelper
import org.apache.isis.client.kroviz.utils.UUID
import pl.treksoft.kvision.core.Component
import pl.treksoft.kvision.core.Overflow
import pl.treksoft.kvision.core.StringPair
import pl.treksoft.kvision.core.onEvent
import pl.treksoft.kvision.form.FormPanel
import pl.treksoft.kvision.form.check.CheckBox
import pl.treksoft.kvision.form.formPanel
import pl.treksoft.kvision.form.range.Range
import pl.treksoft.kvision.form.select.SimpleSelect
import pl.treksoft.kvision.form.spinner.Spinner
import pl.treksoft.kvision.form.text.Password
import pl.treksoft.kvision.form.text.Text
import pl.treksoft.kvision.form.text.TextArea
import pl.treksoft.kvision.form.time.DateTime
import pl.treksoft.kvision.form.time.dateTime
import pl.treksoft.kvision.html.Div
import pl.treksoft.kvision.html.Iframe
import pl.treksoft.kvision.html.Image
import pl.treksoft.kvision.html.image
import pl.treksoft.kvision.panel.VPanel
import pl.treksoft.kvision.panel.vPanel
import pl.treksoft.kvision.require
import pl.treksoft.kvision.utils.auto
import pl.treksoft.kvision.utils.perc
import pl.treksoft.kvision.utils.px

class FormPanelFactory(items: List<FormItem>) : VPanel() {

    var panel: FormPanel<String>?

    init {
        panel = formPanel {
            height = auto
            margin = 10.px
            for (fi: FormItem in items) {
                when (fi.type) {
                    ValueType.TEXT.type -> add(createText(fi))
                    ValueType.PASSWORD.type -> add(createPassword(fi))
                    ValueType.TEXT_AREA.type -> add(createTextArea(fi))
                    ValueType.SIMPLE_SELECT.type -> add(createSelect(fi))
                    ValueType.HTML.type -> add(createHtml(fi))
                    ValueType.NUMERIC.type -> add(createNumeric(fi))
                    ValueType.DATE.type -> add(createDate(fi))
                    ValueType.TIME.type -> add(createTime(fi))
                    ValueType.BOOLEAN.type -> add(createBoolean(fi))
                    ValueType.IMAGE.type -> add(createImage(fi))
                    ValueType.SLIDER.type -> add(createSlider(fi))
                    ValueType.IFRAME.type -> add(createIFrame(fi))
                    ValueType.SVG.type -> add(createSvg(fi))
                }
            }
        }
    }

    private fun createBoolean(fi: FormItem): Component {
        if (fi.content == "true") {
            return CheckBox(label = fi.label, value = true)
        }
        if (fi.content == "false") {
            return CheckBox(label = fi.label, value = false)
        }
        return createText(fi)
    }

    private fun createTime(fi: FormItem): DateTime {
        val date = DateHelper.toDate(fi.content)
        return dateTime(format = "YYYY-MM-DD HH:mm", label = fi.label, value = date)
    }

    private fun createDate(fi: FormItem): DateTime {
        val date = DateHelper.toDate(fi.content)
        return dateTime(
                format = "YYYY-MM-DD",
                label = fi.label,
                value = date
        )
    }

    private fun createNumeric(fi: FormItem): Spinner {
        return Spinner(label = fi.label, value = fi.content as Long)
    }

    private fun createHtml(fi: FormItem): Component {
        return Div(rich = true, content = fi.content.toString())
    }

    private fun createText(fi: FormItem): Text {
        val item = Text(label = fi.label, value = fi.content.toString())
        item.readonly = fi.member?.isReadOnly()
        item.onEvent {
            change = {
                fi.changed(item.value)
                it.stopPropagation()
            }
        }
        return item
    }

    private fun createPassword(fi: FormItem): Password {
        return Password(label = fi.label, value = fi.content as String)
    }

    private fun createTextArea(fi: FormItem): TextArea {
        val rows = fi.size
        val item: TextArea = if (rows != null) {
            val rowCnt = maxOf(3, rows)
            TextArea(label = fi.label, value = fi.content as String, rows = rowCnt)
        } else {
            TextArea(label = fi.label, value = fi.content as String)
        }
        item.readonly = fi.readOnly
        item.onEvent {
            change = {
                fi.changed(item.value)
                it.stopPropagation()
            }
        }
        item.height = 100.perc
        return item
    }

    private fun createSelect(fi: FormItem): SimpleSelect {
        @Suppress("UNCHECKED_CAST")
        val list = fi.content as List<StringPair>
        var preSelectedValue: String? = null
        if (list.isNotEmpty()) {
            preSelectedValue = list.first().first
        }
        return SimpleSelect(label = fi.label, options = list, value = preSelectedValue)
    }

    private fun createImage(fi: FormItem): VPanel {
        val item = VPanel {
            //TODO this is a quick hack, needs to be straightned out
            when {
                fi.callBack is UUID -> {
                    // add InnerPanel to be replaced by callback with svg
                    vPanel {
                        id = (fi.callBack as UUID).value
                    }
                }
                fi.content is Image -> fi.content as Image
                fi.content is String -> {
                    // interpret as (file) URL and load locally
                    val url = fi.content as String
                    console.log("[FPF.createImage]")
                    console.log(url)
                    // passing url as string does not work:
                    // require resolves string to url and `compiles` it into the binary
                    // working with remote resources allows to me more dynamic
                    image(
                            require("img/kroviz-logo.svg"))
                }
                else -> {
                }
            }

        }
        item.height = auto
        item.width = 100.perc
        return item
    }

    private fun createSvg(fi: FormItem): MapPanel {
        val panel = MapPanel()
        panel.height = 100.perc
        panel.width = 100.perc
        fi.callBack = panel
        return panel
    }

    private fun createSlider(fi: FormItem): Range {
        //IMPROVE this needs to be amended for other ranges
        val item = Range(label = fi.label, min = 0, max = 1.0, step = 0.1, value = fi.content as Float)
        item.onEvent {
            change = {
                fi.changed(item.value!!.toString())
                it.stopPropagation()
            }
        }
        return item
    }

    private fun createIFrame(fi: FormItem): VPanel {
        val item = VPanel {
            val url = fi.content as String
            val iframe = Iframe(url)
            iframe.height = 100.perc
            iframe.width = 100.perc
            iframe.overflow = Overflow.INHERIT
            add(iframe)
        }
        item.height = 100.perc
        item.width = 100.perc
        item.overflow = Overflow.INHERIT
        return item
    }

}
