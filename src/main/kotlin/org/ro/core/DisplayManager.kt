package org.ro.core

import org.ro.Application
import org.ro.core.event.EventLog
import org.ro.core.event.LogEntry
import org.ro.core.model.Visible
import org.ro.view.tab.RoTabBar

/**
 * Single point of contact for view components:
 * @item RoView consisting of:
 * @item RoMenubar,
 * @item RoTabbar (RoTabs),
 * @item RoStatusbar,
 * @item Dock
 * etc.
 */

object DisplayManager {

    fun addView(viewable: Visible) {
        val title: String = viewable.tag()
        val le: LogEntry? = EventLog.find(title)
        if (le == null) {
            getTabBar()!!.addView(viewable)
            EventLog.add(title)
        } else {
            le.cacheHits += 1
            EventLog.update(title)
        }
    }

    fun removeView(title: String) {
        EventLog.close(title)
    }

    // view convenience funs
    private fun getTabBar(): RoTabBar? {
        //return Application.view.tabs
        return null
    }

    // view operations

    fun amendMenu() {
        Application.menuBar.amendMenu()
    }
    
}
