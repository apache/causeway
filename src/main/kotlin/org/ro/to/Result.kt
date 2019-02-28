package org.ro.to

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

/**  aka: Services
 */
@Serializable 
data class Result(val value: List<Link> = emptyList(),
                  val links: List<Link> = emptyList(),
                  @Optional val extensions: Extensions? = null) {

    fun valueList(): List<Link> {
        return value
    }
}