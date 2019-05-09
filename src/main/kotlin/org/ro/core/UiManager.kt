package org.ro.core

import org.ro.Application
import org.ro.core.event.EventStore
import org.ro.core.event.LogEntry
import org.ro.core.model.Visible
import org.ro.view.IconManager
import org.ro.view.RoView
import org.ro.view.table.el.EventLogTab
import org.ro.view.table.el.EventLogTable
import pl.treksoft.kvision.i18n.I18n

/**
 * Single point of contact for view components:
 * @item RoView consisting of:
 * @item RoMenubar,
 * @item RoTabbar (RoTabs),
 * @item RoStatusbar,
 * @item Dock
 * etc.
 */

object UiManager {

    fun addView(viewable: Visible) {
        val title: String = viewable.tag()
        val le: LogEntry? = EventStore.findView(title)
        if (le == null) {
            createView()
            EventStore.addView(title)
        } else {
            le.cacheHits += 1
            EventStore.update(title)
        }
    }
    
    fun createView() {
        val title = "Log Entries"
        val icon = IconManager.find(title)
        val tableSpec = EventLogTab().csList
        RoView.addTab(I18n.tr(title), EventLogTable(tableSpec), icon)
    }

    fun removeView(title: String) {
        EventStore.close(title)
    }

    /**
     * Keeps a list of closed/minmized/docked views in order to recreate them.
     * When a tab is 'docked' it can be looked up here.
     * And instead of creating a view a second time, it can be taken from here.
     * setVisible(false) ?
     *
     * A unique id is required in order to be able to look it up and setVisible(true) again.
     */

    fun amendMenu() {
        Application.menuBar.amendMenu()
    }

    fun getMenuItems(): List<MenuEntry> {
        return Menu.list
    }

}
