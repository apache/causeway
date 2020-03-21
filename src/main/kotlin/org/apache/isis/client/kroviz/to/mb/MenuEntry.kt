package org.apache.isis.client.kroviz.to.mb

import kotlinx.serialization.Serializable
import org.apache.isis.client.kroviz.to.TransferObject

@Serializable
data class MenuEntry(
        val menu: List<Menu> = emptyList()
) :TransferObject
