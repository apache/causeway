package org.ro.ui.kv

import org.ro.core.model.DisplayObject
import pl.treksoft.kvision.panel.FlexAlignItems
import pl.treksoft.kvision.panel.FlexWrap
import pl.treksoft.kvision.panel.HPanel
import pl.treksoft.kvision.panel.VPanel
import pl.treksoft.kvision.utils.px

class RoDisplay(displayObject: DisplayObject) : VPanel() {

    init {
        val model = displayObject.data

        HPanel(
                FlexWrap.NOWRAP,
                alignItems = FlexAlignItems.CENTER,
                spacing = 20) {
            padding = 10.px
        }
    }

}
