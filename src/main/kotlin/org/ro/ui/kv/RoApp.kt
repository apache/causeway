package org.ro.org.ro.ui.kv

import org.ro.ui.RoStatusBar
import org.ro.ui.kv.RoMenuBar
import org.ro.ui.kv.RoView
import pl.treksoft.kvision.panel.SimplePanel

object RoApp: SimplePanel(){
    init {
        this.add(RoMenuBar.navbar)
        this.add(RoView.tabPanel)
        this.add(RoStatusBar.navbar)
    }
}
