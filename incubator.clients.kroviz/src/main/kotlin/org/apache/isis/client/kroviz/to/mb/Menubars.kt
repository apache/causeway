package org.apache.isis.client.kroviz.to.mb

import kotlinx.serialization.Serializable
import org.apache.isis.client.kroviz.to.TransferObject

@Serializable
data class Menubars(val primary:org.apache.isis.client.kroviz.to.mb.MenuEntry,
                    val secondary:org.apache.isis.client.kroviz.to.mb.MenuEntry,
                    val tertiary:org.apache.isis.client.kroviz.to.mb.MenuEntry,
                    val metadataError: String? = null
) :org.apache.isis.client.kroviz.to.TransferObject
