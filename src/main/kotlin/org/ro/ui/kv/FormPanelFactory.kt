package org.ro.ui.kv

import org.ro.ui.FormItem
import pl.treksoft.kvision.core.StringPair
import pl.treksoft.kvision.core.onEvent
import pl.treksoft.kvision.form.FormPanel
import pl.treksoft.kvision.form.formPanel
import pl.treksoft.kvision.form.select.SimpleSelect
import pl.treksoft.kvision.form.text.Password
import pl.treksoft.kvision.form.text.Text
import pl.treksoft.kvision.form.text.TextArea
import pl.treksoft.kvision.panel.VPanel
import pl.treksoft.kvision.utils.px

class FormPanelFactory(items: List<FormItem>) : VPanel() {

    var panel: FormPanel<String>?

    init {
        panel = formPanel {
            margin = 10.px
            for (fi: FormItem in items) {
                when (fi.type) {
                    "Text" -> add(createText(fi))
                    "Password" -> add(createPassword(fi))
                    "TextArea" -> add(createTextArea(fi))
                    "SimpleSelect" -> add(createSelect(fi))
                }
            }
        }
    }

    private fun createText(fi: FormItem): Text {
        val item = Text(label = fi.label, value = fi.content as String)
        item.readonly = fi.member?.isReadOnly()
        item.onEvent {
            change = {
                fi.changed()
                it.stopPropagation()
            }
        }
        return item
    }

    private fun createPassword(fi: FormItem): Password {
        return Password(label = fi.label, value = fi.content as String)
    }

    private fun createTextArea(fi: FormItem): TextArea {
        val rowCnt = maxOf(3, fi.size)
        val item = TextArea(label = fi.label, value = fi.content as String, rows = rowCnt)
        item.readonly = fi.readOnly
        item.onEvent {
            change = {
                fi.changed()
                it.stopPropagation()
            }
        }
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
