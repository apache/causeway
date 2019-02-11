package org.ro.generated

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable
import org.ro.to.Extensions
import org.ro.to.Link
import org.ro.to.Parameter

@Serializable  // is Property a Variant of Member?
data class Property(val id: String = "",
                    val memberType: String = "",
                    val value: String? = null,
                    val format: String? = null,
                    val extensions: Extensions? = null,
                    val disabledReason: String? = null,
                    val optional: Boolean? = null,
                    val links: List<Link>? = null,
                    @Optional val parameters: List<Parameter> = emptyList(),
                    @Optional val maxLength: Int = 0) {

    val DESCRIBED_BY = "describedby"

    fun descriptionLink(): Link? {
        for (l in links!!) {
            if (l.rel == DESCRIBED_BY)
                return l
        }
        return null
    }
}