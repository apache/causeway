package org.ro.layout

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable
import org.ro.view.HBox
import org.ro.view.UIComponent
import org.ro.view.UIUtil

@Serializable
data class ColLayout(val domainObject: String? = null,
                     val metadataError: String? = null,
                     val cssClass: String? = null,
                     val size: Int? = null,
                     val id: String? = null,
                     val span: Int = 0,
                     val unreferencedActions: String? = null,
                     val unreferencedCollections: String? = null,
                     @Optional val tabGroup: List<TabLayout> = emptyList(),
                     @Optional val fieldSet: List<FieldSetLayout> = emptyList()) {

    fun build(): HBox {
        val result = HBox()
        UIUtil().decorate(result, "ColLayout", "debug")
        var b: UIComponent?
        for (tl in tabGroup) {
            b = tl.build()
            result.addChild(b)
        }
        for (fsl in fieldSet) {
            b = fsl.build()
            result.addChild(b)
        }
        // actions will not be rendered as buttons
        return result
    }

}