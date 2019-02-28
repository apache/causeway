package org.ro.to

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

enum class MemberType(val type: String) {
    ACTION("action"),
    PROPERTY("property"),
    COLLECTION("collection")
}

@Serializable
data class Member(val id: String,
                  val memberType: String,
                  @Optional val links: List<Link> = emptyList(),
        //TODO is a custom serializer required?
/* value can be one of <null | String.class | Link.class> */
                  // = "" satisfies 2 more tests than = null !!!
//                  @Optional val value: String = "", 
                  @Optional val extensions: Extensions? = null,
                  @Optional val disabledReason: String = "",
                  @Optional val format: String = "",
                  @Optional val optional: Boolean = false) {

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
