package org.ro.to

import kotlinx.serialization.Serializable

@Serializable
//TODO value can be a
// -> list of links  or
// -> string (cf. ACTIONS_OPEN_SWAGGER_UI)
data class Result(
        val value: List<Link> = emptyList(),
        val links: List<Link> = emptyList(),
        val extensions: Extensions? = null
) : TransferObject {

    fun getValueLinks(): List<Link> {
        return value
    }
}
