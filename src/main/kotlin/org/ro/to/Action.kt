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
        for (l in links) {
            if (l.rel.indexOf(id) > 0) {
                return l
            }
        }
        return null
    }

    fun findParameterByName(name: String): Parameter? {
        for (p in parameters) {
            if (p.value.id == name) return p.value
        }
        return null
    }

}
