package org.apache.isis.client.kroviz.to

import kotlinx.serialization.Serializable

@Serializable
data class HttpError(
        val httpStatusCode: Int,
        val message: String,
        val detail:org.apache.isis.client.kroviz.to.HttpErrorDetail? = null
) :org.apache.isis.client.kroviz.to.TransferObject
