package org.apache.isis.client.kroviz.to

import kotlinx.serialization.Serializable

@Serializable
data class Icon(val image: String) : TransferObject
