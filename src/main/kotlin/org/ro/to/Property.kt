package org.ro.to

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable
import org.ro.core.TransferObject

@Serializable  // is Property a Variant of Member?
data class Property(val id: String = "",
                    val memberType: String = "",
                    val links: List<Link> = emptyList(),
                    @Optional val optional: Boolean? = null,
                    @Optional val title: String? = null,
                    @Optional val value: String? = null,
                    val extensions: Extensions? = null,
                    @Optional val format: String? = null,
                    @Optional val disabledReason: String? = null,
                    @Optional val parameters: List<Parameter> = emptyList(),
                    @Optional val maxLength: Int = 0) : TransferObject {

    fun descriptionLink(): Link? {
        for (l in links) {
            if (l.rel == RelType.DESCRIBEDBY.type)  
                return l
        }
        return null
    }
}