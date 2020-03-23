package org.apache.isis.client.kroviz.to.mb

import kotlinx.serialization.Serializable
import org.apache.isis.client.kroviz.to.TransferObject

@Serializable
data class Section(
        val serviceAction: List<ServiceAction> = emptyList()
) : TransferObject
