package org.ro.to.mb

import kotlinx.serialization.Serializable
import org.ro.to.TransferObject

@Serializable
data class Menubars(val primary: MenuEntry,
                    val secondary: MenuEntry,
                    val tertiary: MenuEntry,
                    val metadataError: String? = null
) : TransferObject
