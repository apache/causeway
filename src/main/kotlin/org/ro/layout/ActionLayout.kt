package org.ro.layout

import kotlinx.serialization.Serializable
import org.ro.to.Link

@Serializable
data class ActionLayout(val named: String? = "",
                        val describedAs: String? = "",
                        val metadataError: String? = "",
                        val link: Link? = null,
                        val id: String? = "",
                        val bookmarking: String? = "",
                        val cssClass: String? = "",
                        val cssClassFa: String? = "",
                        val cssClassFaPosition: String? = "",
                        val hidden: String? = null,
                        val namedEscaped: String? = "",
                        val position: String? = "",
                        val promptStyle: String? = "")