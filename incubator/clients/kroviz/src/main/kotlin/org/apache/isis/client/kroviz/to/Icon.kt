package org.apache.isis.client.kroviz.to

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.w3c.dom.Image

@Serializable
data class Icon(@Contextual val image: Image) : TransferObject
