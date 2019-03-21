package org.ro.to

import kotlinx.serialization.Serializable

@Serializable
data class HttpErrorDetail(
        val className: String,
        val message: String,
        val element: List<String>,
        var causedBy: String? = null
) {
    init {
        if (causedBy == null) causedBy = "null"
    }
}