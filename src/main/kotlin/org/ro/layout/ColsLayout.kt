package org.ro.layout

import kotlinx.serialization.Serializable
import org.ro.view.uicomp.Box
import org.ro.view.uicomp.VBox

@Serializable
data class ColsLayout(val col: ColLayout? = null) {
    fun build(): VBox {
        val result = VBox("ColsLayout/tab")
        val b: Box = col!!.build()
        result.addChild(b)
        return result
    }
}

 