package org.apache.isis.client.kroviz.to

import kotlinx.serialization.Serializable

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

    var type: String? = ValueType.TEXT.type

    init {
        if (memberType == MemberType.PROPERTY.type
                && value == null
                && extensions != null
                && extensions.xIsisFormat == "string") {
            value = Value("")
        }
        type = TypeMapper().match(this)
    }

    fun isReadOnly(): Boolean {
        return !isReadWrite()
    }

    fun isReadWrite(): Boolean {
        return (memberType == MemberType.PROPERTY.type && disabledReason == "")
    }

}
