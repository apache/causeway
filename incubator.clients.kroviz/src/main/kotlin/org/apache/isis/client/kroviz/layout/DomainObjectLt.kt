package org.apache.isis.client.kroviz.layout

import kotlinx.serialization.Serializable
import org.apache.isis.client.kroviz.to.Link

@Serializable
data class DomainObjectLt(val named: String? = null,
                          val describedAs: String? = null,
                          val plural: String? = null,
                          val metadataError: String? = null,
                          val link:Link? = null,
                          val bookmarking: String? = null,
                          val cssClass: String? = null,
                          val cssClassFa: String? = null,
                          val cssClassFaPosition: String? = null,
                          val namedEscaped: Boolean? = false)
