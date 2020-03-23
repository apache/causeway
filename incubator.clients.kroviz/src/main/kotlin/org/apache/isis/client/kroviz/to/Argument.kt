package org.apache.isis.client.kroviz.to

import kotlinx.serialization.Serializable

//IMPROVE initialize value="", move behavior out of TO
@Serializable
//TODO either have kotlinx.serialization cope with empty key or implement custom serialization (cf. to.Value)
data class Argument(var key: String = "",
                    var value: String? = null,
                    val potFileName: String = "",
                    val href: String? = null) :org.apache.isis.client.kroviz.to.TransferObject {
    init {
        if (value == null) {
            value = ""
        }
    }

}
