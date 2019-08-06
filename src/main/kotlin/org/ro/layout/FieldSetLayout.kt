package org.ro.layout

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable
import org.ro.view.uicomp.Form
import org.ro.view.uicomp.FormItem
import org.ro.view.uicomp.HBox

@Serializable
@Suppress("DEPRECATION")
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
        val form: Form = Form("new Form")
        for (p in property) {
            val label = p.named ?: "label not set"
            val type = "Text"// if mutiline use a different type of input p.multiLine
            val content = "sample content"
            fi = FormItem(label, type, content)
            form.addElement(fi)
        }
        result.addChild(form)
        return result
    }

}
