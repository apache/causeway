package org.ro.to

import kotlinx.serialization.Optional
import kotlinx.serialization.SerialId
import kotlinx.serialization.Serializable

@Serializable
data class Member(@SerialId(1) val id: String = "",
                  @SerialId(2) @Optional val links: List<Link> = emptyList(),
                  @SerialId(3) val memberType: String,
                  @SerialId(4) @Optional val value: String? = null,
                  @SerialId(5) @Optional val format: String? = null,
                  @SerialId(6) val extensions: Extensions? = null,
                  @SerialId(7) @Optional val disabledReason: String? = null,
                  @SerialId(8) @Optional val optional: Boolean = false) {

    private fun isString(): Boolean {
        return (format == "string") || (extensions!!.xIsisFormat == "string")
    }

    private fun isNumber(): Boolean {
        return (format == "int") || (format == "utc-millisec")
    }

}
