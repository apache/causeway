package org.ro.layout

import kotlinx.serialization.Serializable
import org.ro.to.Link
import org.ro.to.bs3.Col

@Serializable
data class ColLayout(val domainObject: DomainObjectLayout? = null,
                     val fieldSet: MutableList<FieldSetLayout> = mutableListOf<FieldSetLayout>(),
                     val action: MutableList<ActionLayout> = mutableListOf<ActionLayout>(),  // org.ro.authors
                     val collection: MutableList<CollectionLayout> = mutableListOf<CollectionLayout>(),
                     val metadataError: String? = "",
                     val cssClass: String? = "",
                     val size: String? = "",
                     val id: String? = "",
                     val span: Int? = 0,
                     val unreferencedActions: Boolean? = false,
                     val unreferencedCollections: Boolean? = false,
                     val named: String? = "",
                     val describedAs: String? = "",
                     val plural: String? = "",
                     val link: Link? = null,
                     val bookmarking: String? = "",
                     val cssClassFa: String? = "",
                     val cssClassFaPosition: String? = "",
                     val namedEscaped: Boolean? = false,
                     val tabGroup: MutableList<TabGroupLayout> = mutableListOf<TabGroupLayout>()
) {
    constructor(col: Col) : this() {
        col.tabGroups.forEach {
            tabGroup.add(TabGroupLayout(it))
        }
        val fs = col.fieldSet!!
        fieldSet.add(FieldSetLayout(fs))
    }

}
