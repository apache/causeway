package org.ro.ui.kv

import org.ro.ui.RoStatusBar
import pl.treksoft.kvision.panel.SimplePanel

object RoApp : SimplePanel() {
    init {
        this.add(RoMenuBar.navbar)
        this.add(RoView.tabPanel)
        this.add(RoStatusBar.navbar)
    }
}
