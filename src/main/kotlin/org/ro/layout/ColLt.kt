package org.ro.layout

import kotlinx.serialization.Serializable
import org.ro.to.Link

@Serializable
data class ColLt(val domainObject: DomainObjectLt? = null,
                 val row: List<RowLt> = mutableListOf<RowLt>(),
                 val fieldSet: List<FieldSetLt> = mutableListOf<FieldSetLt>(),
                 val action: List<ActionLt> = mutableListOf<ActionLt>(),
                 val collection: List<CollectionLt> = mutableListOf<CollectionLt>(),
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
                 val tabGroup: List<TabGroupLt> = mutableListOf<TabGroupLt>()
)
