package org.apache.isis.client.kroviz.to.mb

import kotlinx.serialization.Serializable
import org.apache.isis.client.kroviz.to.TransferObject

@Serializable
data class Menu(val named: String,
                val cssClassFa: String? = null,
                val section: List<Section> = emptyList(),
                val unreferencedActions: Boolean? = false
) : TransferObject
