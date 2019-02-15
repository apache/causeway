package org.ro.layout

import kotlinx.serialization.Serializable
import org.ro.view.Box
import org.ro.view.UIUtil
import org.ro.view.VBox

@Serializable
data class ColsLayout(val col: ColLayout? = null) {
    fun build(): VBox {
        val result = VBox()
//        result.label = "tab: $id"
        UIUtil().decorate(result, "RowLayout", "debug")
        var b: Box
        /*    for (c in cols) {
                b = c.build()
                result.addChild(b)
            }  */
        return result
    }
}

 