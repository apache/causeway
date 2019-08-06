package org.ro.layout

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable
import org.ro.to.Link

@Serializable
@Suppress("DEPRECATION")
data class PropertyLayout(val named: String? = null,
                          val describedAs: String? = null,
                          @Optional val action: List<ActionLayout> = emptyList(),
                          var metadataError: String? = null,
                          val link: Link? = null,
                          val id: String? = null,
                          val cssClass: String? = null,
                          val hidden: String? = null,  //ALL_TABLES
                          val labelPosition: String? = null,
                          val multiLine: Int? = 1,
                          val namedEscaped: Boolean? = false,
                          val promptStyle: String? = null,
                          val renderedAsDayBefore: Boolean? = false,
                          val typicalLength: Int? = null,
                          val unchanging: String? = null
)
