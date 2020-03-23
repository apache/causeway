package org.apache.isis.client.kroviz.to

import kotlinx.serialization.Serializable

@Serializable
data class Version(val links: List<Link> = emptyList(),
                   val specVersion: String = "",
                   val implVersion: String = "",
                   val optionalCapabilities: Map<String, String> = emptyMap(),
                   val extensions: Extensions? = null
) : TransferObject
