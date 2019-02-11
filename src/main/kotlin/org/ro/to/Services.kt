package org.ro.to

import kotlinx.serialization.SerialId
import kotlinx.serialization.Serializable

@Serializable
data class Services(@SerialId(1) val value: List<Link> = emptyList(),
                    @SerialId(2) val extensions: Extensions? = null,
                    @SerialId(3) val links: List<Link> = emptyList()) {
    fun valueList(): List<Link> {
        return value
    }
}