package org.apache.isis.client.kroviz.to.mb

import kotlinx.serialization.Serializable
import org.apache.isis.client.kroviz.to.TransferObject

@Serializable
data class Menubars(val primary: MenuEntry,
                    val secondary: MenuEntry,
                    val tertiary: MenuEntry,
                    val metadataError: String? = null
) : TransferObject
