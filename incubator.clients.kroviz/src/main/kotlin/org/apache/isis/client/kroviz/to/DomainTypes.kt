package org.apache.isis.client.kroviz.to

import kotlinx.serialization.Serializable

@Serializable
data class DomainTypes(val links: List<Link> = emptyList(),
                       val values: List<Link> = emptyList(),
                       val extensions: Extensions? = null
) : TransferObject
