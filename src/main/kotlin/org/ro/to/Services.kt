package org.ro.to

import kotlinx.serialization.Serializable

@Serializable
data class Services(val value: List<Link> = emptyList(),
                    val extensions: Extensions? = null,
                    val links: List<Link> = emptyList()) {
    
    fun valueList(): List<Link> {
        return value
    }
}