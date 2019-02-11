package org.ro.to

import kotlinx.serialization.Serializable

@Serializable
class Action(val id: String,
             val memberType: String,
             val links: List<Link> = emptyList(),
             val parameters: List<Parameter> = emptyList(),
             val extensions: Extensions) {

    fun getInvokeLink(): Link? {
        for (l in this.links) {
            if (l.rel.indexOf(this.id) > 0) {
                return l
            }
        }
        return null
    }

    fun findParameterByName(name: String): Parameter? {
        for (p in this.parameters) {
            if (p.id == name) return p
        }
        return null
    }

}