package org.ro.view

import org.ro.view.tab.RoTabBar

class RoView() {
    var menuBar: RoMenuBar? = null
        get
    var dock: Dock? = null
        get
    var tabs: RoTabBar? = null
        get
    var statusBar: RoStatusBar? = null
        get

    init {
        menuBar = RoMenuBar()
        tabs = RoTabBar()
        dock = Dock()
        statusBar = RoStatusBar()
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
            statusBar = RoStatusBar()
        } else {
//            this.removeChild(statusBar)
            //this.invalidateDisplayList()
        }
    }

}