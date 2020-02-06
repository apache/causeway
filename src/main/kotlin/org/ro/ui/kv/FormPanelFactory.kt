package org.ro.ui.kv

import org.ro.core.Utils
import org.ro.to.ValueType
import org.ro.ui.FormItem
import pl.treksoft.kvision.core.Component
import pl.treksoft.kvision.core.StringPair
import pl.treksoft.kvision.core.onEvent
import pl.treksoft.kvision.form.FormPanel
import pl.treksoft.kvision.form.check.CheckBox
import pl.treksoft.kvision.form.formPanel
import pl.treksoft.kvision.form.select.SimpleSelect
import pl.treksoft.kvision.form.spinner.Spinner
import pl.treksoft.kvision.form.text.Password
import pl.treksoft.kvision.form.text.Text
import pl.treksoft.kvision.form.text.TextArea
import pl.treksoft.kvision.form.time.DateTime
import pl.treksoft.kvision.form.time.dateTime
import pl.treksoft.kvision.html.Div
import pl.treksoft.kvision.html.header
import pl.treksoft.kvision.panel.SimplePanel
import pl.treksoft.kvision.panel.VPanel
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
                }
            }
        }
    }

    private fun createBoolean(fi: FormItem): Component {
        if (fi.content is Boolean) {
            val item = CheckBox(label = fi.label, value = fi.content as Boolean)
            return item
        } else {
            return createText(fi)
        }
    }

    private fun createTime(fi: FormItem): DateTime {
        val date = Utils.toDate(fi.content)
        val item = dateTime(format = "YYYY-MM-DD HH:mm", label = fi.label, value = date)
        return item
    }

    private fun createDate(fi: FormItem): DateTime {
        val date = Utils.toDate(fi.content)
        val item = dateTime(
                format = "YYYY-MM-DD",
                label = fi.label,
                value = date
        )
        return item
    }

    private fun createNumeric(fi: FormItem): Spinner {
        val item = Spinner(label = fi.label, value = fi.content as Long)
        return item
    }

    //FIXME the outer frame needs to be moved to the builder
    private fun createHtml(fi: FormItem): Component {
        val cpt = SimplePanel()
        cpt.header(fi.label).addCssClass("panel-heading")
        cpt.title = fi.label
        val div = Div(rich = true, content = fi.content.toString())
        div.addCssClass("panel-body")
        cpt.add(div)
        return cpt
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
//       val rowCnt = maxOf(3, fi.size)
        val item = TextArea(label = fi.label, value = fi.content as String)
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

}
