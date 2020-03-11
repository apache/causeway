package org.ro.layout

import kotlinx.serialization.Serializable

@Serializable
data class ColsLt(val col: ColLt? = null) {

    fun getCol(): ColLt {
        // return getValues().first()
//        console.log("[layout/Cols.getCol]")
//        console.log(col)
        //       return col.first()
        return col!!
    }

    //fun getValues(): MutableCollection<Col> {
    //FIXME wrapper in between required?
    // return col.
    // }

}
