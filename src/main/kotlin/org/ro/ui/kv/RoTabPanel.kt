package org.ro.ui.kv

import pl.treksoft.kvision.panel.SimplePanel
import pl.treksoft.kvision.panel.TabPanel
import pl.treksoft.kvision.panel.VPanel

class RoTabPanel : TabPanel() {

    override fun removeTab(index: Int): TabPanel {
        val tab = getChildComponent(index)
        RoView.removeTab(tab as SimplePanel)
        return super.removeTab(index)
    }

    fun findTab(title: String) : Int? {
        val kids = getChildren()
        console.log("[RoTabPanel.findTab]")
        console.log(kids)
        kids.forEachIndexed { index, component ->
            when (component) {
                is VPanel -> if (component.title == title) {
                    return index
                }
            }
        }
        return null
    }

}
