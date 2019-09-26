package org.ro.layout

import kotlinx.serialization.Serializable
import org.ro.ui.uicomp.Box
import org.ro.ui.uicomp.VBox

@Serializable
data class RowLayout(val cols: List<ColsLayout> = emptyList(),
                     val metadataError: String? = null,
                     val cssClass: String? = null,
                     val id: String? = null
) {
//    private val maxSpan = 12

    fun build(): VBox {
        val result = VBox("RowLayout")
        var b: Box
        for (c in cols) {
            b = c.build()
            result.addChild(b)
        }
        return result
    }

}
