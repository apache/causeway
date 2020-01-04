package org.ro.to.mb

import kotlinx.serialization.Serializable
import org.ro.to.TransferObject

@Serializable
data class Section(
        val serviceAction: List<ServiceAction> = emptyList()
) : TransferObject
