package org.ro.layout

import kotlinx.serialization.Serializable
import org.ro.to.bs3.Col

@Serializable
data class ColsLayout(var col: ColLayout? = null) {

    constructor(c: Col) : this() {
        col = ColLayout(c)
    }

}

