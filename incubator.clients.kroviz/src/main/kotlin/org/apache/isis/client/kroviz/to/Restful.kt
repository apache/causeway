package org.apache.isis.client.kroviz.to

import kotlinx.serialization.Serializable

@Serializable
data class Restful(val links: List<Link> = emptyList(),
                   val extensions: Extensions
) : TransferObject
