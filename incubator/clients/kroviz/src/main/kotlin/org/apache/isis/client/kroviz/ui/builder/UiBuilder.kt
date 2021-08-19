package org.apache.isis.client.kroviz.ui.builder

import io.kvision.core.CssSize
import io.kvision.core.UNIT
import io.kvision.panel.SimplePanel

abstract class UiBuilder {

    protected fun style(panel: SimplePanel) {
        panel.width = CssSize(100, UNIT.perc)
        panel.height = CssSize(100, UNIT.perc)
    }

}
