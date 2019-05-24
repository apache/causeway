package org.ro.to

import kotlinx.serialization.Serializable
import org.ro.core.TransferObject

@Serializable
data class Service(val links: List<Link> = emptyList(),
                   val extensions: Extensions? = null,
                   val title: String = "",
                   val serviceId: String = "",
                   val members: Map<String, Member> = emptyMap()
) : TransferObject 