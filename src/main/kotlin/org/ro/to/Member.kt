package org.ro.to

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable
import org.ro.core.TransferObject

@Serializable
data class Member(val id: String,
                  val memberType: String,
                  @Optional val links: List<Link> = emptyList(),
                  //FIXME
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

    private fun isString(): Boolean {
        return (format == "string") || (extensions!!.xIsisFormat == "string")
    }

    private fun isNumber(): Boolean {
        return (format == "int") || (format == "utc-millisec")
    }

}
