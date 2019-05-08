package org.ro

import org.ro.view.RoMenuBar
import org.ro.view.RoStatusBar
import org.ro.view.RoView
import pl.treksoft.kvision.hmr.ApplicationBase
import pl.treksoft.kvision.panel.Root
import pl.treksoft.kvision.panel.VPanel.Companion.vPanel
import pl.treksoft.kvision.require
import pl.treksoft.kvision.utils.px


object Application : ApplicationBase {

    private lateinit var root: Root
    var menuBar = RoMenuBar
    var view = RoView()
    var statusBar = RoStatusBar()

    override fun start(state: Map<String, Any>) {

        root = Root("showcase") {
            vPanel(spacing = 0) {
                padding = 0.px
                this.add(menuBar.navbar)
                this.add(view) 
                this.add(statusBar) 
            }
        }
    }

    override fun dispose(): Map<String, Any> {
        root.dispose()
        return mapOf()
    }

    val css = require("css/kroviz.css")
}
