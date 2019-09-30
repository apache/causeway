package org.ro.to

import kotlinx.serialization.Serializable

@Serializable
//FIXME value can be a
// a list of links  or
// a string (cf. ACTIONS_OPEN_SWAGGER_UI)
data class Result(val value: List<Link> = emptyList(),
                  val links: List<Link> = emptyList(),
                  val extensions: Extensions? = null
) : TransferObject
