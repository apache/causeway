package org.ro.to

import kotlinx.serialization.Serializable
import org.ro.to.Extensions
import org.ro.to.Link
import org.ro.to.TransferObject

@Serializable
data class User(val userName: String = "",
                val roles: List<String> = emptyList(),
                val links: List<Link> = emptyList(),
                val extensions: Extensions? = null

) : TransferObject
