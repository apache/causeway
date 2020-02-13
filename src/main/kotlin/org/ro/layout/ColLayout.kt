package org.ro.layout

import kotlinx.serialization.Serializable
import org.ro.to.Link

@Serializable
data class ColLayout(val domainObject: DomainObjectLayout? = null,
                     val row: MutableList<RowLayout> = mutableListOf<RowLayout>(),
                     val fieldSet: MutableList<FieldSetLayout> = mutableListOf<FieldSetLayout>(),
                     val action: MutableList<ActionLayout> = mutableListOf<ActionLayout>(),
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
)
