package org.ro.ui.builder

import org.ro.layout.FieldSetLayout
import org.ro.to.TObject
import org.ro.ui.FormItem
import org.ro.ui.kv.FormPanelFactory
import org.ro.ui.kv.RoDisplay
import pl.treksoft.kvision.form.FormPanel

class FieldSetBuilder {

    fun create(fieldSetLayout: FieldSetLayout, tObject: TObject, tab: RoDisplay): FormPanel<String>? {
        val members = tObject.getProperties()
        val items = mutableListOf<FormItem>()
        console.log("[FSB.create] property")
        console.log(fieldSetLayout.property)
        for (p in fieldSetLayout.property) {
            val label = p.id ?: "label not set"
            var type = "Text"
            if (p.multiLine.asDynamic() != null) {
                type = "TextArea"
            }
            var content: Any = ""
            val member = members.firstOrNull() { it.id == label }
            if (member != null) {
                content = member.value?.content.toString()
            }
            //TODO handle numbers, dates, etc. as well
            if (content is String) {
                if (content.startsWith("<") && content.endsWith(">")) {
                    type = "Html"
                }
            }
            if (content == "true") {
                content = true
                type = "Boolean"
            }
            if (content == "false") {
                content = false
                type = "Boolean"
            }

            if (member?.extensions?.xIsisFormat == "number") {
                content = content as Number
                type = "Numeric"
            }

            val description = p.describedAs
            val fi = FormItem(label, type, content,
                    description = description,
                    member = member,
                    tab = tab)
            items.add(fi)
        }
        return FormPanelFactory(items).panel
    }

}
