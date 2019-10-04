package org.ro.to

import kotlinx.serialization.Serializable

@Serializable
data class Action(val id: String,
                  val memberType: String,
                  val links: List<Link> = emptyList(),
                  val parameters: Map<String, Parameter> = emptyMap(),
                  val extensions: Extensions
) : TransferObject {

    fun getInvokeLink(): Link? {
        return links.firstOrNull { it.rel.indexOf(id) > 0 }
    }

    fun findParameterByName(name: String): Parameter? {
        return parameters.values.firstOrNull { it.id == name }
    }

}
