package org.apache.isis.client.kroviz

import org.apache.isis.client.kroviz.ui.kv.RoApp
import pl.treksoft.kvision.Application
import pl.treksoft.kvision.pace.Pace
import pl.treksoft.kvision.panel.root
import pl.treksoft.kvision.panel.vPanel
import pl.treksoft.kvision.require
import pl.treksoft.kvision.startApplication
import pl.treksoft.kvision.utils.px

class App : Application() {

    init {
        require("css/kroviz.css")
    }

    override fun start() {
        Pace.init()
        root("kroviz") {
            vPanel(spacing = 0) {
                padding = 0.px
                add(RoApp)
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
