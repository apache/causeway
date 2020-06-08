package org.apache.isis.client.kroviz.ui.kv

import pl.treksoft.kvision.panel.HPanel
import pl.treksoft.kvision.panel.SimplePanel

object RoApp : SimplePanel() {
    init {
        this.add(RoMenuBar.navbar)

        val view = HPanel()
        view.add(RoToolPanel.panel)
        view.add(RoView.tabPanel)
        this.add(view)

        this.add(RoStatusBar.navbar)
    }
}
