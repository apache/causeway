package org.apache.causeway.client.kroviz.to

import kotlinx.serialization.Serializable
import org.apache.causeway.client.kroviz.to.Menu
import org.apache.causeway.client.kroviz.to.TransferObject

@Serializable
data class MenuEntry(
        val menu: List<Menu> = emptyList()
) : TransferObject