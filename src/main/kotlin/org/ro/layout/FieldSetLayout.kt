package org.ro.layout

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable
import org.ro.view.FormItem
import org.ro.view.HBox

@Serializable
data class FieldSetLayout(val name: String? = null,
                          @Optional val action: List<ActionLayout> = emptyList(),
                          @Optional val property: List<PropertyLayout> = emptyList(),
                          val metadataError: String? = null,
                          val id: String? = null,
                          val unreferencedActions: Boolean? = false,
                          @Optional val unreferencedCollections: Boolean? = false,
                          val unreferencedProperties: Boolean? = false) {

    fun build(): HBox {
        val result = HBox("FieldSetLayout")
        var fi: FormItem?
        val form: org.ro.view.Form = org.ro.view.Form("new Form")
        for (p in property) {
            fi = FormItem(p.toString())
            form.addElement(fi)
        }
        result.addChild(form)
        return result
    }

}