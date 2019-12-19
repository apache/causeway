package org.ro.layout

import kotlinx.serialization.Serializable
import org.ro.to.Link
import org.ro.to.bs3.Col
import org.ro.ui.uicomp.HBox
import org.ro.ui.uicomp.UIComponent

@Serializable
data class ColLayout(val domainObject: DomainObjectLayout? = null,
                     val action: MutableList<ActionLayout> = mutableListOf<ActionLayout>(),  // org.ro.authors
                     val named: String? = "",
                     val describedAs: String? = "",
                     val plural: String? = "",
                     val link: Link? = null,
                     val bookmarking: String? = "",
                     val metadataError: String? = "",
                     val cssClass: String? = "",
                     val cssClassFa: String? = "",
                     val cssClassFaPosition: String? = "",
                     val namedEscaped: Boolean? = false,
                     val size: String? = "",
                     val id: String? = "",
                     val span: Int? = 0,
                     val unreferencedActions: Boolean? = false,
                     val unreferencedCollections: Boolean? = false,
                     val tabGroup: MutableList<TabGroupLayout> = mutableListOf<TabGroupLayout>(),
                     val fieldSet: MutableList<FieldSetLayout> = mutableListOf<FieldSetLayout>()
) {
    constructor(col: Col) : this() {
        col.tabGroups.forEach {
            tabGroup.add(TabGroupLayout(it))
        }
        val fs = col.fieldSet!!
        fieldSet.add(FieldSetLayout(fs))
    }

    fun build(): HBox {
        val result = HBox("ColLayout")
        var b: UIComponent
        for (tgl in tabGroup) {
            b = tgl.build()
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
