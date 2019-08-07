package org.ro.view

import pl.treksoft.kvision.core.Component
import pl.treksoft.kvision.core.CssSize
import pl.treksoft.kvision.core.UNIT
import pl.treksoft.kvision.panel.TabPanel
import pl.treksoft.kvision.panel.VPanel

object RoView : TabPanel() {
    private var tabCount = 0

    init {
        marginTop = CssSize(50, UNIT.px)
    }

    fun addTab(
            title: String,
            panel: Component) {

        val icon = IconManager.find(title)

        super.addTab(
                title,
                panel,
                icon,
                image = null,
                closable = true)
        activeIndex = tabCount
        tabCount += 1
    }

    fun removeTab(tab: VPanel) {
        super.remove(tab)
        tabCount--
    }

}
