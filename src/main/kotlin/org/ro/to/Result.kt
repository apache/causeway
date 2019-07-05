package org.ro.to

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

@Serializable
data class Result(val value: List<Link> = emptyList(),
                  val links: List<Link> = emptyList(),
                  @Optional val extensions: Extensions? = null
) : TransferObject
