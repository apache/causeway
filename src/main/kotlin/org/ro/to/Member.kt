package org.ro.to

import kotlinx.serialization.Serializable
import org.ro.core.Utils

@Serializable
data class Member(val id: String,
                  val memberType: String,
                  val links: List<Link> = emptyList(),
        //IMROVE: make value immutable (again) and handle property edits eg. via a wrapper
        // members of type property have a value, those of type action don't
                  var value: Value? = null,
                  val format: String = "",
                  val extensions: Extensions? = null,
                  val disabledReason: String = "",
                  val optional: Boolean = false
) : TransferObject {

    init {
        if (memberType == MemberType.PROPERTY.type
                && value == null
                && extensions != null
                && extensions.xIsisFormat == "string") {
            value = Value("")
        }
    }

    fun isReadOnly(): Boolean {
        console.log("[Member.isReadOnly] $id disabledReason = '$disabledReason'")
        return !isReadWrite()
    }

    fun isReadWrite(): Boolean {
        return (memberType == MemberType.PROPERTY.type && disabledReason == "")
    }

    fun isHtml(): Boolean {
        val s = value!!.content
        return if (s is String) {
            Utils.isXml(s)
        } else {
            false
        }
    }

    fun isBoolean(): Boolean {
        val content = value?.content.toString()
        if (content == "true") {
            return true
        }
        if (content == "false") {
            return true
        }
        return false
    }

    fun isNumeric(): Boolean {
        return when {
            format == "int" -> true
            else -> false
        }
    }


}
