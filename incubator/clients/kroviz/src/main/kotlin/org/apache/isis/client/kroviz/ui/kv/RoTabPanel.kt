package org.apache.isis.client.kroviz.ui.kv

import pl.treksoft.kvision.core.CssSize
import pl.treksoft.kvision.core.UNIT
import pl.treksoft.kvision.panel.SimplePanel
import pl.treksoft.kvision.panel.TabPanel
import pl.treksoft.kvision.panel.VPanel

class RoTabPanel : TabPanel() {

    init {
        this.width = CssSize(100, UNIT.perc)
    }

    override fun removeTab(index: Int): TabPanel {
        val tab = getChildComponent(index)
        RoView.removeTab(tab as SimplePanel)
        return super.removeTab(index)
    }

    fun findTab(title: String): Int? {
        getChildren().forEachIndexed { index, component ->
            if ((component is VPanel) && (component.title == title)) {
                return index
            }
        }
        return null
    }

}
