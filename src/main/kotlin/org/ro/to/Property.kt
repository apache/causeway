package org.ro.to

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

@Serializable  // TODO are Property and Action subclasses of Member?
@Suppress("DEPRECATION")
data class Property(val id: String = "",
                    val memberType: String = "",
                    val links: List<Link> = emptyList(),
                    @Optional val optional: Boolean? = null,
                    @Optional val title: String? = null,
                    @Optional val value: Value? = null,
                    @Optional val extensions: Extensions? = null,
                    @Optional val format: String? = null,
                    @Optional val disabledReason: String? = null,
                    @Optional val parameters: List<Parameter> = emptyList(),
                    @Optional val maxLength: Int = 0
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
