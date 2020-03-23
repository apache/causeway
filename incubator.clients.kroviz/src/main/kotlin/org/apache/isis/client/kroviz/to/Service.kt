package org.apache.isis.client.kroviz.to

import kotlinx.serialization.Serializable

@Serializable
data class Service(val links: List<Link> = emptyList(),
                   val extensions: Extensions? = null,
                   val title: String = "",
                   val serviceId: String = "",
                   val members: Map<String, Member> = emptyMap()
) : TransferObject
