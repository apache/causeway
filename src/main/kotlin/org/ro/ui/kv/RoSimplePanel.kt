package org.ro.ui.kv

import org.ro.core.model.DisplayModel
import pl.treksoft.kvision.panel.SimplePanel

class RoSimplePanel(displayable: DisplayModel) : SimplePanel() {
    init {
        title = displayable.extractTitle()
    }

}
