package org.ro.layout

import kotlinx.serialization.Serializable
import org.ro.to.bs3.Col
import org.ro.ui.uicomp.Box
import org.ro.ui.uicomp.VBox

@Serializable
data class ColsLayout(var col: ColLayout? = null) {

    constructor(c: Col) : this() {
        col = ColLayout(c)
    }

    fun build(): VBox {
        val result = VBox("ColsLayout/tab")
        val b: Box = col!!.build()
        result.addChild(b)
        return result
    }
}

