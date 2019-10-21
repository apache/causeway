package org.ro

import org.ro.ui.RoStatusBar
import org.ro.ui.kv.RoMenuBar
import org.ro.ui.kv.RoView
import pl.treksoft.kvision.hmr.ApplicationBase
import pl.treksoft.kvision.panel.Root
import pl.treksoft.kvision.panel.VPanel.Companion.vPanel
import pl.treksoft.kvision.utils.px

object Application : ApplicationBase {

    val css = pl.treksoft.kvision.require("css/kroviz.css")
    private lateinit var root: Root

    override fun start(state: Map<String, Any>) {
        root = Root("showcase") {
            vPanel(spacing = 0) {
                padding = 0.px
                this.add(RoMenuBar.navbar)
                this.add(RoView.tabPanel)
                this.add(RoStatusBar.navbar)
            }
        }
    }

    override fun dispose(): Map<String, Any> {
        root.dispose()
        return mapOf()
    }

}
