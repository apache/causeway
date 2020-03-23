package org.apache.isis.client.kroviz.to

import kotlinx.serialization.Serializable

@Serializable
data class HttpErrorDetail(
        val className: String,
        val message: String? = null,
        val element: List<String>,
        var causedBy:org.apache.isis.client.kroviz.to.HttpErrorDetail? = null
) :org.apache.isis.client.kroviz.to.TransferObject
