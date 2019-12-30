package org.ro.ui.kv

import org.ro.core.model.DisplayObject
import pl.treksoft.kvision.panel.VPanel

class RoDisplay(displayObject: DisplayObject) : VPanel() {

    init {
        val ol = displayObject.layout
        if (ol != null) {
            val model = displayObject.data!!
            val tObject = model.delegate
            val objectPanel = ol.build(tObject)
            add(objectPanel)
        }
    }

}
