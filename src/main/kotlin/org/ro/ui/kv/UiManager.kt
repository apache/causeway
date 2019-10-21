package org.ro.ui.kv

import org.ro.core.aggregator.BaseAggregator
import org.ro.core.aggregator.IAggregator
import org.ro.core.aggregator.UndefinedAggregator
import org.ro.core.event.EventStore
import org.ro.core.event.LogEntry
import org.ro.core.model.DisplayList
import org.ro.core.model.DisplayObject
import org.ro.ui.RoStatusBar
import pl.treksoft.kvision.panel.VPanel

/**
 * Single point of contact for view components consisting of:
 * @item RoMenubar,
 * @item RoView (tabs, etc.),
 * @item RoStatusbar,
 */
object UiManager {

    fun add(title: String, panel: VPanel, aggregator: IAggregator = UndefinedAggregator()) {
        RoView.addTab(title, panel)
        EventStore.addView(title, aggregator)
    }

    fun closeView(tab: VPanel) {
        EventStore.closeView(tab.title!!)
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

    fun openListView(aggregator: BaseAggregator) {
        val displayable = aggregator.dsp
        val title: String = displayable!!.extractTitle()
        val panel = RoTable(displayable as DisplayList)
        add(title, panel, aggregator)
        displayable.isRendered = true
    }

    fun openObjectView(aggregator: BaseAggregator) {
        val displayable = aggregator.dsp
        val title: String = displayable!!.extractTitle()
        val panel = RoDisplay(displayable as DisplayObject)
        add(title, panel, aggregator)
        displayable.isRendered = true
    }

}
