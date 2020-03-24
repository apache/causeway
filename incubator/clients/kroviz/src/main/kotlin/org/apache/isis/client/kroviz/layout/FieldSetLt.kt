package org.apache.isis.client.kroviz.layout

import kotlinx.serialization.Serializable

@Serializable
data class FieldSetLt(val name: String? = null,
                      val action: List<ActionLt> = emptyList(),
                      val property: List<PropertyLt> = emptyList(),
                      val metadataError: String? = null,
                      val id: String? = null,
                      val unreferencedActions: Boolean? = false,
                      val unreferencedCollections: Boolean? = false,
                      val unreferencedProperties: Boolean? = false
)
