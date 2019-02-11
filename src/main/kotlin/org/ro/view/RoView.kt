package org.ro.view

import kotlinx.serialization.ImplicitReflectionSerializer
import org.ro.view.tab.RoTabBar
import pl.treksoft.kvision.core.Border
import pl.treksoft.kvision.core.BorderStyle
import pl.treksoft.kvision.core.Col
import pl.treksoft.kvision.panel.FlexDir
import pl.treksoft.kvision.panel.FlexPanel
import pl.treksoft.kvision.panel.FlexWrap
import pl.treksoft.kvision.panel.TabPanel
import pl.treksoft.kvision.utils.px
import pl.treksoft.kvision.utils.vh

@ImplicitReflectionSerializer
class RoView() : FlexPanel() {
    
    private var tabPanel = TabPanel {
        border = Border(2.px, BorderStyle.SOLID, Col.SILVER)
//        addTab(I18n.tr("Basic formatting"), EditPanel(), "fa-bars", route = "/basic")
    }
    
    init {
        flexPanel(FlexDir.COLUMN, FlexWrap.WRAP, spacing = 20) {
            padding = 0.px
            paddingTop = 50.px
            height = 100.vh
            add(tabPanel)
        }
    }
    var menuBar: RoMenuBar? = null
        get
    var dock: Dock? = null
        get
    var tabs: RoTabBar? = null
        get

    init {
        menuBar = RoMenuBar()
        tabs = RoTabBar()
        dock = Dock()
//        statusBar = RoStatusBar()
//        this.addElement(statusBar)
    }

    fun showDock(toggle: Boolean): Unit {
        if (toggle) {
            dock = Dock()
        } else {
//            body.removeChild(dock)
            //          body.invalidateDisplayList()
        }
    }

    fun showStatus(toggle: Boolean): Unit {
        if (toggle) {
            //statusBar = RoStatusBar()
        } else {
//            this.removeChild(statusBar)
            //this.invalidateDisplayList()
        }
    }

}