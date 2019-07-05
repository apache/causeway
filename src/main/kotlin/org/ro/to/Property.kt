package org.ro.to

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

@Serializable  // TODO are Property and Action subclasses of Member?
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
}
