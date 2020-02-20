package org.ro.layout

import kotlinx.serialization.Serializable
import org.ro.to.Link

@Serializable
data class DomainObject(val named: String? = null,
                        val describedAs: String? = null,
                        val plural: String? = null,
                        val metadataError: String? = null,
                        val link: Link? = null,
                        val bookmarking: String? = null,
                        val cssClass: String? = null,
                        val cssClassFa: String? = null,
                        val cssClassFaPosition: String? = null,
                        val namedEscaped: Boolean? = false)
