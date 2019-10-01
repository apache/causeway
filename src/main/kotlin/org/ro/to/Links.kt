package org.ro.to

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Links(
        @SerialName("links") val content: List<Link> = emptyList()
) : TransferObject

