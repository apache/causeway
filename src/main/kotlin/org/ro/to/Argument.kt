package org.ro.to

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

@Serializable
data class Argument(@Optional val key: String = "",
                    val value: String? = null,
                    @Optional val potFileName: String = "")
