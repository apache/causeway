package org.ro.layout

import kotlinx.serialization.Serializable
import org.ro.to.Link

@Serializable
data class ColLt(val domainObject: DomainObjectLt? = null,
                 val row: List<RowLt> = emptyList(),
                 val fieldSet: List<FieldSetLt> = emptyList(),
                 val action: List<ActionLt> = emptyList(),
                 val collection: List<CollectionLt> = emptyList(),
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
                 val tabGroup: List<TabGroupLt> = emptyList()
)
