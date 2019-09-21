package org.ro.view

import pl.treksoft.kvision.core.*
import pl.treksoft.kvision.panel.TabPanel
import pl.treksoft.kvision.panel.VPanel


object RoView {
    val tabPanel = TabPanel()
    private var tabCount = 0

    init {
        tabPanel.marginTop = CssSize(48, UNIT.px)
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

    fun updatePowered(by: String) {
        val color = Col.GOLD
        val bg = Background(
                //image = "fa fa-cube"
                color = color
        )
        tabPanel.background = bg
    }

}
