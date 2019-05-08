package org.ro.to

import kotlinx.serialization.Serializable
import org.ro.core.TransferObject

@Serializable
data class HttpErrorDetail(
        val className: String,
        val message: String,
        val element: List<String>,
        var causedBy: String? = null
) : TransferObject {
    init {
        if (causedBy == null) causedBy = "null"
    }
}