package org.ro.to.mb

import kotlinx.serialization.Serializable
import org.ro.to.TransferObject

@Serializable
data class Menu(val named: String,
                val cssClassFa: String? = null,
                val section: List<Section> = emptyList(),
                val unreferencedActions: Boolean? = false
) : TransferObject
