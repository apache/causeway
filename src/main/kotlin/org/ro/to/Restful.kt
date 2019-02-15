package org.ro.to

import kotlinx.serialization.Serializable

@Serializable
data class Restful(val extensions: Extensions? = null,
                   val links: List<Link>? = null)