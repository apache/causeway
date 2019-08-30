package org.ro.view

import pl.treksoft.kvision.core.Component
import pl.treksoft.kvision.core.CssSize
import pl.treksoft.kvision.core.UNIT
import pl.treksoft.kvision.panel.TabPanel
import pl.treksoft.kvision.panel.VPanel

object RoView {
    val tabPanel = TabPanel()
    private var tabCount = 0

    init {
        tabPanel.marginTop = CssSize(48, UNIT.px)
        tabPanel.height = CssSize(100, UNIT.perc)
    }

    fun addTab(
            title: String,
            panel: Component) {

        val icon = IconManager.find(title)

        tabPanel.addTab(
                title,
                panel,
                icon,
                image = null,
                closable = true)
        tabPanel.activeIndex = tabCount
        tabCount += 1
    }

    fun removeTab(tab: VPanel) {
        tabPanel.remove(tab)
        tabCount--
    }

}
