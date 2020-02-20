package org.ro.layout

import kotlinx.serialization.Serializable

@Serializable
data class Cols(val col: Col? = null) {

    fun getCol(): Col {
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

