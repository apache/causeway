package org.ro.org.ro.to

import kotlinx.serialization.Serializable
import org.ro.to.Extensions
import org.ro.to.Link
import org.ro.to.TransferObject

@Serializable
data class Version(val links: List<Link> = emptyList(),
                   val specVersion: String = "",
                   val implVersion: String = "",
                   val optionalCapabilities: Map<String, String> = emptyMap(),
                   val extensions: Extensions? = null
) : TransferObject
