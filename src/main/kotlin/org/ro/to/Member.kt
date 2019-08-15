package org.ro.to

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

@Serializable
@Suppress("DEPRECATION")
data class Member(val id: String,
                  val memberType: String,
                  @Optional val links: List<Link> = emptyList(),
                  // members of type action do not have a value whereas those of type property have it !!!
                  @Optional val value: Value? = null,
                  @Optional val format: String = "",
                  @Optional val extensions: Extensions? = null,
                  @Optional val disabledReason: String = "",
                  @Optional val optional: Boolean = false
) : TransferObject {

    fun getInvokeLink(): Link? {
        for (l in links) {
            if (l.rel.indexOf(id) > 0) {
                return l
            }
        }
        return null
    }

}
