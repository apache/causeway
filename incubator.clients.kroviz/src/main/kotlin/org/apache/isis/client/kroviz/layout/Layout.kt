package org.apache.isis.client.kroviz.layout

import kotlinx.serialization.Serializable
import org.apache.isis.client.kroviz.to.TransferObject

/**
 * Json variant of layout TransferObject
 *
 * @See: https://en.wikipedia.org/wiki/Composite_pattern
 */
@Serializable
data class Layout(val cssClass: String? = null,
                  val row: List<RowLt> = emptyList()) : TransferObject
