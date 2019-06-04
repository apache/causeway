package org.ro.view

import pl.treksoft.kvision.core.Component
import pl.treksoft.kvision.core.CssSize
import pl.treksoft.kvision.core.UNIT
import pl.treksoft.kvision.panel.TabPanel

object RoView : TabPanel() {
    init {
        marginTop = CssSize(50, UNIT.px)
    }

    fun addTab(
            title: String, 
            panel: Component): TabPanel {
        
        val icon = IconManager.find(title)
        
        val result = super.addTab(
                title,
                panel,
                icon,
                image=null,
                closable = true)
        return result
    }
}