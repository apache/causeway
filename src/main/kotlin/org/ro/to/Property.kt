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
) : TransferObject
