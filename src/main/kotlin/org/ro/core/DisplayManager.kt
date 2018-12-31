package org.ro.core

import org.ro.core.event.EventLog
import org.ro.core.event.LogEntry
import org.ro.core.model.Visible
import org.ro.view.IDockable
import org.ro.view.RoMenuBar
import org.ro.view.RoView
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

    fun addView(viewable: Visible): Unit {
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
    private fun getView(): RoView? {
        return Globals.view
    }

    private fun getMenuBar(): RoMenuBar? {
        return getView()!!.menuBar
    }

    private fun getTabBar(): RoTabBar? {
        return getView()!!.tabs
    }

    // view operations
    fun updateStatus(le: LogEntry?) {
        getView()!!.statusBar!!.update(le)
    }

    fun amendMenu(menu: Menu) {
        getMenuBar()!!.amend(menu)
    }

    fun dockView(tab: IDockable) {
        getView()!!.dock!!.addView(tab)
    }

    fun getMenu(): Menu? {
        return getMenuBar()!!.getMenu()
    }

    fun setMenu(menu: Menu) {
        getMenuBar()!!.setMenu(menu)
    }

    // delegate to Tabs
    fun addEventTab() {
        var list: MutableList<LogEntry>? = EventLog.getEntries()
        getTabBar()!!.addEventTab(list)
    }

    fun addTreeTab() {
        var list: MutableList<LogEntry>? = EventLog.getEntries()
        getTabBar()!!.addTreeTab(list)
    }

    fun toggleDock(toggle: Boolean) {
        getView()!!.showDock(toggle)
    }

    fun toggleStatus(toggle: Boolean) {
        getView()!!.showStatus(toggle)
    }

}
