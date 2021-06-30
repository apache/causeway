package org.apache.isis.client.kroviz.to

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.w3c.files.Blob

@Serializable
data class Icon(@Contextual val image: Blob) : TransferObject
