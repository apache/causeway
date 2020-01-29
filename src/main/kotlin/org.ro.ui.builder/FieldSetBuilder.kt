package org.ro.ui.builder

import org.ro.layout.FieldSetLayout
import org.ro.to.TObject
import org.ro.to.TypeMapperType
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
                if (p.multiLine.asDynamic() != null) {
                    member.type = TypeMapperType.TEXT_AREA.type
                }

                val fi = FormItem(
                        label = label,
                        type = member.type!!,
                        content = member.value?.content,
                        description = p.describedAs,
                        member = member,
                        tab = tab)
                items.add(fi)
            }
        }
        return FormPanelFactory(items).panel
    }

}
