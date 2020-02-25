package org.ro.to

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Extensions(val oid: String = "",
                      val isService: Boolean = false,
                      val isPersistent: Boolean = false,
                      val menuBar: String? = MenuBarPosition.PRIMARY.position,
                      val actionSemantics: String? = null,
                      val actionType: String = "",
                      @SerialName("x-isis-format") val xIsisFormat: String? = null,
                      val friendlyName: String = "",
                      val collectionSemantics: String? = null,
                      val pluralName: String = "",
                      val description: String = ""
) : TransferObject
