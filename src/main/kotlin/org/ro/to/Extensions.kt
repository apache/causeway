package org.ro.to

import kotlinx.serialization.Optional
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Extensions(@Optional val oid: String = "",
                      @Optional val isService: Boolean = false,
                      @Optional val isPersistent: Boolean = false,
                      @Optional val menuBar: String? = MenuBarPosition.PRIMARY.position,
                      @Optional val actionSemantics: String? = null,
                      @Optional val actionType: String = "",
                      @Optional @SerialName("x-isis-format") val xIsisFormat: String? = null,
                      @Optional val friendlyName: String = "",
                      @Optional val collectionSemantics: String? = null) : TransferObject
