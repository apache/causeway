package org.ro.ui.kv

import org.ro.core.model.DisplayObject
import org.ro.ui.uicomp.FormItem
import pl.treksoft.kvision.core.StringPair
import pl.treksoft.kvision.form.FormPanel
import pl.treksoft.kvision.form.formPanel
import pl.treksoft.kvision.form.select.SimpleSelect
import pl.treksoft.kvision.form.text.Password
import pl.treksoft.kvision.form.text.Text
import pl.treksoft.kvision.form.text.TextArea
import pl.treksoft.kvision.panel.VPanel
import pl.treksoft.kvision.utils.px

class RoDisplay(displayObject: DisplayObject) : VPanel() {

    var panel: FormPanel<String>?

    init {
        val model = displayObject.data!!
        val items: MutableList<FormItem> = mutableListOf<FormItem>()
        val tObject = model.delegate
        console.log("[RoDisplay]")
        console.log(tObject)
        tObject.members.entries.forEach { it ->
            val label = it.key
            val type = "Text" //it.value.memberType
            val content = it.key //it.value.value
            val item = FormItem(label, type, content)
            console.log(item)
            items.add(item)
        }

        panel = formPanel {
            margin = 10.px
            for (fi: FormItem in items) {
                when (fi.type) {
                    "Text" -> {
                        add(Text(label = fi.label, value = fi.content as String))
                    }
                    "Password" -> {
                        add(Password(label = fi.label, value = fi.content as String))
                    }
                    "TextArea" -> {
                        val rowCnt = maxOf(3, fi.size)
                        add(TextArea(label = fi.label, value = fi.content as String, rows = rowCnt))
                    }
                    "SimpleSelect" -> {
                        @Suppress("UNCHECKED_CAST")
                        val list = fi.content as List<StringPair>
                        var preSelectedValue: String? = null
                        if (list.isNotEmpty()) {
                            preSelectedValue = list.first().first
                        }
                        val select = SimpleSelect(label = fi.label, options = list, value = preSelectedValue)
                        add(select)
                    }
                }
            }
        }

    }
}
