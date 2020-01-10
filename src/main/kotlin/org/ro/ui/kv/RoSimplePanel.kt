package org.ro.ui.kv

import org.ro.core.model.BaseDisplayable
import pl.treksoft.kvision.panel.SimplePanel

class RoSimplePanel(displayable: BaseDisplayable) : SimplePanel() {
    init {
        title = displayable.extractTitle()
    }

}
