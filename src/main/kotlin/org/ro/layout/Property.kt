package org.ro.layout

import kotlinx.serialization.Serializable
import org.ro.to.Link

@Serializable
data class Property(val named: String? = null,
                    val describedAs: String? = null,
                    val action: List<Action> = emptyList(),
                    var metadataError: String? = null,
                    val link: Link? = null,
                    val id: String? = null,
                    val cssClass: String? = null,
                    val hidden: String? = null,  //ALL_TABLES
                    val labelPosition: String? = null,
                    val multiLine: Int? = 1,
                    val namedEscaped: Boolean? = false,
                    val promptStyle: String? = null,
                    val renderDay: Boolean? = false,
                    val renderedAsDayBefore: String? = null,   //always omitted with 2.0.0?
                    val typicalLength: Int? = null,
                    val repainting: String? = null,
                    val unchanging: String? = null
)
