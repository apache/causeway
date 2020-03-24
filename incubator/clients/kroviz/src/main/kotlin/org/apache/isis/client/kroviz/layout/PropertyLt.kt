package org.apache.isis.client.kroviz.layout

import kotlinx.serialization.Serializable
import org.apache.isis.client.kroviz.to.Link

@Serializable
data class PropertyLt(val named: String? = null,
                      val describedAs: String? = null,
                      val action: List<ActionLt> = emptyList(),
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
