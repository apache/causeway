package org.ro.ui.kv

import org.ro.ui.RoView
import pl.treksoft.kvision.panel.TabPanel
import pl.treksoft.kvision.panel.VPanel

class RoTabPanel : TabPanel() {

    override fun removeTab(index: Int): TabPanel {
        val tab = getChildComponent(index)
        RoView.removeTab(tab as VPanel)
        return super.removeTab(index)
    }

}
