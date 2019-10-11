package org.ro.to

import kotlinx.serialization.Serializable

@Serializable
data class HttpError(
        val httpStatusCode: Int,
        val message: String,
        val detail: HttpErrorDetail? = null
) : TransferObject
