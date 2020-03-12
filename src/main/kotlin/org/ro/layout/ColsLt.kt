package org.ro.layout

import kotlinx.serialization.Serializable

@Serializable
data class ColsLt(val col: ColLt) {

    fun getCol(): ColLt {
        return col//.first()
    }

}
