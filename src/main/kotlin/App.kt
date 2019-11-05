package org.ro

import org.ro.ui.RoStatusBar
import org.ro.ui.kv.RoMenuBar
import org.ro.ui.kv.RoView
import pl.treksoft.kvision.Application
import pl.treksoft.kvision.pace.Pace
import pl.treksoft.kvision.panel.SimplePanel
import pl.treksoft.kvision.panel.root
import pl.treksoft.kvision.panel.vPanel
import pl.treksoft.kvision.require
import pl.treksoft.kvision.startApplication
import pl.treksoft.kvision.utils.px

class App : Application() {
    var vPanel:SimplePanel? = null

    init {
        require("css/kroviz.css")
    }

    override fun start() {
        Pace.init()
        root("showcase") {
            vPanel = vPanel(spacing = 0) {
                padding = 0.px
                this.add(RoMenuBar.navbar)
                this.add(RoView.tabPanel)
                this.add(RoStatusBar.navbar)
            }
        }
    }

    override fun dispose(): Map<String, Any> {
        return mapOf()
    }
}

fun main() {
    startApplication(::App)
}
