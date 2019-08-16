package org.ro.to

import kotlinx.serialization.Serializable

@Serializable
data class Property(val id: String = "",
                    val memberType: String = "",
                    val links: List<Link> = emptyList(),
                    val optional: Boolean? = null,
                    val title: String? = null,
                    val value: Value? = null,
                    val extensions: Extensions? = null,
                    val format: String? = null,
                    val disabledReason: String? = null,
                    val parameters: List<Parameter> = emptyList(),
                    val maxLength: Int = 0
) : TransferObject {

    fun descriptionLink(): Link? {
        val answer = links.find {
            it.rel == RelType.DESCRIBEDBY.type
        }
        return answer
    }

    /**
     * property-description's have extensions.friendlyName whereas
     * plain properties don't have them  cf.:
     * FR_PROPERTY_DESCRIPTION
     * FR_OBJECT_PROPERTY_
     */
    fun isPropertyDescription(): Boolean {
        val hasExtensions = extensions != null
        if (!hasExtensions) {
            return false
        }
        val hasFriendlyName = extensions!!.friendlyName.isNotEmpty()
        return hasFriendlyName
    }
}
