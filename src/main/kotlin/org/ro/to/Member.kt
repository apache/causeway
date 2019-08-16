package org.ro.to

import kotlinx.serialization.Serializable

@Serializable
data class Member(val id: String,
                  val memberType: String,
                  val links: List<Link> = emptyList(),
        // members of type property have a value, those of type action don't
                  val value: Value? = null,
                  val format: String = "",
                  val extensions: Extensions? = null,
                  val disabledReason: String = "",
                  val optional: Boolean = false
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
