package org.ro.layout

import kotlinx.serialization.Serializable
import org.ro.to.bs3.Col
import pl.treksoft.kvision.panel.HPanel
import pl.treksoft.kvision.panel.VPanel

@Serializable
data class ColsLayout(var col: ColLayout? = null) {

    constructor(c: Col) : this() {
        col = ColLayout(c)
    }

    fun build(): VPanel {
        val result = VPanel()
        val b: HPanel = col!!.build()
        result.add(b)
        return result
    }
}

