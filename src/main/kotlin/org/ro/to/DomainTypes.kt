package org.ro.to

import kotlinx.serialization.Serializable
import org.ro.to.Extensions
import org.ro.to.Link
import org.ro.to.TransferObject

@Serializable
data class DomainTypes(val links: List<Link> = emptyList(),
                   val values: List<Link> = emptyList(),
                   val extensions: Extensions? = null
) : TransferObject
