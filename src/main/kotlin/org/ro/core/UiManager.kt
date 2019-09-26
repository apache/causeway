package org.ro.core

import org.ro.core.event.EventStore
import org.ro.core.event.LogEntry
import org.ro.core.model.DisplayList
import org.ro.ui.RoMenuBar
import org.ro.ui.RoStatusBar
import org.ro.ui.RoView
import org.ro.ui.table.RoTable
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
        RoMenuBar.amendMenu()
    }

    fun updateStatus(entry: LogEntry) {
        RoStatusBar.update(entry)
    }

    fun updateUser(user: String) {
        RoStatusBar.updateUser(user)
    }

    fun updatePower(by: String) {
        RoView.updatePowered(by)
        RoStatusBar.brand("#FF00FF")
       // https://www.w3schools.com/css/css3_gradients.asp
      //  #grad {
        //    background-image: linear-gradient(to right, red,orange,yellow,green,blue,indigo,violet);
       // }
    }

    fun handleView(displayList: DisplayList) {
        val title: String = extractTitle(displayList)
        val panel = RoTable(displayList)
        add(title, panel)
        displayList.isRendered = true
    }

    private fun extractTitle(displayList: DisplayList): String {
        val strList = displayList.title.split("/")
        val len = strList.size
        return strList.get(len - 2)
    }

}
