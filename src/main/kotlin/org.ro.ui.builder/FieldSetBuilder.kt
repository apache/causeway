package org.ro.ui.builder

import org.ro.layout.FieldSetLayout
import org.ro.to.TObject
import org.ro.to.ValueType
import org.ro.ui.FormItem
import org.ro.ui.kv.FormPanelFactory
import org.ro.ui.kv.RoDisplay
import pl.treksoft.kvision.form.FormPanel

class FieldSetBuilder {

    fun create(
            fieldSetLayout: FieldSetLayout,
            tObject: TObject,
            tab: RoDisplay
    ): FormPanel<String>? {
        val members = tObject.getProperties()
        val items = mutableListOf<FormItem>()
        console.log("[FSB.create] property")
        console.log(fieldSetLayout.property)
        for (p in fieldSetLayout.property) {
            val label = p.id ?: "label not set"

            val member = members.firstOrNull() { it.id == label }

            if (member != null) {
                var size = 1
                console.log("[FSB.create] p.multiline")
                console.log(p.multiLine)
                if (p.multiLine != null && p.multiLine > 1) {
                    member.type = ValueType.TEXT_AREA.type
                    size = p.multiLine
                }

                val fi = FormItem(
                        label = label,
                        type = member.type!!,
                        content = member.value?.content,
                        size = size,
                        description = p.describedAs,
                        member = member,
                        tab = tab)
                items.add(fi)
            }
        }
        return FormPanelFactory(items).panel
    }

}
