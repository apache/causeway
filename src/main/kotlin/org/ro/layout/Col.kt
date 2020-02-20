package org.ro.layout

import kotlinx.serialization.Serializable
import org.ro.to.Link

@Serializable
data class Col(val domainObject: DomainObject? = null,
               val row: List<Row> = emptyList(),
               val fieldSet: List<FieldSet> = emptyList(),
               val action: List<Action> = emptyList(),
               val collection: List<Collection> = emptyList(),
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
               val tabGroup: List<TabGroup> = emptyList()
)
