package org.ro.layout

import kotlinx.serialization.Serializable
import org.ro.to.TObject
import org.ro.to.bs3.FieldSet
import org.ro.ui.FormItem
import org.ro.ui.kv.FormPanelFactory
import org.ro.ui.kv.RoDisplay
import pl.treksoft.kvision.form.FormPanel

@Serializable
data class FieldSetLayout(val name: String? = null,
                          val action: MutableList<ActionLayout> = mutableListOf<ActionLayout>(),
                          val property: MutableList<PropertyLayout> = mutableListOf<PropertyLayout>(),
                          val metadataError: String? = null,
                          val id: String? = null,
                          val unreferencedActions: Boolean? = false,
                          val unreferencedCollections: Boolean? = false,
                          val unreferencedProperties: Boolean? = false
) {
    constructor(fieldSet: FieldSet) : this() {
        fieldSet.actions.forEach {
            action.add(ActionLayout(it))
        }
        fieldSet.properties.forEach {
            property.add(PropertyLayout(it))
        }
    }

    fun build(tObject: TObject, tab: RoDisplay): FormPanel<String>? {
        val members = tObject.getProperties()
        val items = mutableListOf<FormItem>()
        console.log("[FSL.build] property")
        console.log(property)
        for (p in property) {
            val label = p.id ?: "label not set"
            var type = "Text"
            if (p.multiLine.asDynamic() != null) {
                type = "TextArea"
            }
            //TODO handle numbers, dates, etc. as well
            var content = ""
            val member = members.firstOrNull() { it.id == label }
            if (member != null) {
                content = member.value?.content.toString()
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
