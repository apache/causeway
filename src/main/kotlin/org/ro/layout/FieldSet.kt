package org.ro.layout

import kotlinx.serialization.Serializable

@Serializable
data class FieldSet(val name: String? = null,
                    val action: List<Action> = emptyList(),
                    val property: List<Property> = emptyList(),
                    val metadataError: String? = null,
                    val id: String? = null,
                    val unreferencedActions: Boolean? = false,
                    val unreferencedCollections: Boolean? = false,
                    val unreferencedProperties: Boolean? = false
)
