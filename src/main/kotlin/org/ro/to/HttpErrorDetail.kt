package org.ro.to

import kotlinx.serialization.Serializable

@Serializable
data class HttpErrorDetail(
        val className: String,
        val message: String? = null,
        val element: List<String>,
        var causedBy: HttpErrorDetail? = null
) : TransferObject
