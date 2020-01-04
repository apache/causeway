package org.ro.to.mb

import kotlinx.serialization.Serializable
import org.ro.to.Link
import org.ro.to.TransferObject

@Serializable
data class ServiceAction(
        val objectType: String? = null,
        val id: String? = null,
        val named: String? = null,
        val namedEscaped: String? = null,
        val bookmarking: String? = null,
        val cssClass: String? = null,
        val cssClassFa: String? = null,
        val describedAs: String? = null,
        val metadataError: String? = null,
        val link: Link? = null
) : TransferObject
