package org.apache.isis.client.kroviz.to

import kotlinx.serialization.Serializable

@Serializable
data class User(val userName: String = "",
                val roles: List<String> = emptyList(),
                val links: List<Link> = emptyList(),
                val extensions: Extensions? = null
) : TransferObject
