package org.ro.layout

import kotlinx.serialization.Serializable

@Serializable
data class ColsLt(val col: ColLt) {

    fun getColList(): List<ColLt> {
        val colList = mutableListOf<ColLt>()
        colList.add(col)
        return colList
    }

    fun getCol(): ColLt {
        return getColList().first()
    }

}
