package org.apache.isis.client.kroviz.ui.kv

import org.apache.isis.client.kroviz.to.ValueType
import org.apache.isis.client.kroviz.ui.FormItem
import org.apache.isis.client.kroviz.utils.DateHelper
import pl.treksoft.kvision.core.Component
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
import pl.treksoft.kvision.panel.VPanel
import pl.treksoft.kvision.panel.vPanel
import pl.treksoft.kvision.utils.perc
import pl.treksoft.kvision.utils.px

class FormPanelFactory(items: List<FormItem>) : VPanel() {

    var panel: FormPanel<String>?

    init {
        panel = formPanel {
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
        val item: TextArea
        if (rows != null) {
            val rowCnt = maxOf(3, rows)
            item = TextArea(label = fi.label, value = fi.content as String, rows = rowCnt)
        } else {
            item = TextArea(label = fi.label, value = fi.content as String)
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
        val item = VPanel(spacing = 3) {
            // add InnerPanel to be replaced by callback with svg
            vPanel { id = fi.callBackId }
        }
        item.height = 100.perc
        item.width = 100.perc
        return item
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

}
