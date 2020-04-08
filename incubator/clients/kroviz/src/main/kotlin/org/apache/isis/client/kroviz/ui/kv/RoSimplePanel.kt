package org.apache.isis.client.kroviz.ui.kv

import org.apache.isis.client.kroviz.core.model.DisplayModel
import pl.treksoft.kvision.panel.SimplePanel

class RoSimplePanel(displayable: DisplayModel) : SimplePanel() {
    init {
        title = displayable.extractTitle()
    }

}
