package org.apache.isis.client.kroviz.to.mb

import kotlinx.serialization.Serializable
import org.apache.isis.client.kroviz.to.Link
import org.apache.isis.client.kroviz.to.TransferObject

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
        val link:org.apache.isis.client.kroviz.to.Link? = null
) :org.apache.isis.client.kroviz.to.TransferObject
