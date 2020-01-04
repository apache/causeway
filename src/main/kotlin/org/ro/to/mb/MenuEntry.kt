package org.ro.to.mb

import kotlinx.serialization.Serializable
import org.ro.to.TransferObject

@Serializable
data class MenuEntry(
        val menu: List<Menu> = emptyList()
) : TransferObject
