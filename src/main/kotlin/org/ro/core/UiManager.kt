package org.ro.core

import org.ro.Application
import org.ro.core.event.EventStore
import org.ro.core.event.LogEntry
import org.ro.core.model.DisplayList
import org.ro.core.model.MemberExposer
import org.ro.core.model.ResultExposer
import org.ro.view.RoStatusBar
import org.ro.view.RoView
import org.ro.view.table.DynamicTable
import org.ro.view.table.TableFactory
import org.ro.view.table.fr.FixtureResultTable
import pl.treksoft.kvision.panel.VPanel

/**
 * Single point of contact for view components consisting of:
 * @item RoMenubar,
 * @item RoView (tabs, etc.),
 * @item RoStatusbar,
 */
object UiManager {

    fun add(title: String, panel: VPanel) {
        RoView.addTab(title, panel)
        EventStore.addView(title)
    }

    fun remove(tab: VPanel) {
        RoView.removeTab(tab)
        // EventStore.close(tab.get)
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

    fun updateStatus(entry: LogEntry) {
        RoStatusBar.update(entry)
    }

    fun updateUser(user: String) {
        RoStatusBar.updateUser(user)
    }

    fun handleView(displayList: DisplayList) {
        if (displayList.title.contains("FixtureScript", ignoreCase = false)) {
            handleFixtureResult(displayList)
        } else {
            handleDynamic(displayList)
        }
    }

    private fun handleDynamic(displayList: DisplayList) {
        val members = displayList.getMembers()
        val columns = TableFactory().buildColumns(members)
        val panel = DynamicTable(displayList.getData() as List<MemberExposer>, columns)
        add(displayList.title, panel)
        displayList.isRendered = true
    }

    @Deprecated("use generic / dynamic table")
    private fun handleFixtureResult(displayList: DisplayList) {
        val title: String = this::class.simpleName.toString()
        @Suppress("UNCHECKED_CAST")
        val panel = FixtureResultTable(displayList.getData() as List<ResultExposer>)
        add(title, panel)
        displayList.isRendered = true
    }

}
