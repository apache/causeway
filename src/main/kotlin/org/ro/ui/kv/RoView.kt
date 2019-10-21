package org.ro.ui.kv

import org.ro.ui.kv.UiManager
import org.ro.ui.IconManager
import pl.treksoft.kvision.core.*
import pl.treksoft.kvision.panel.VPanel

/**
 * Area between menu bar at the top and the status bar at the bottom.
 * Contains:
 * @Item TabPanel with Tabs
 */
object RoView {
    val tabPanel = RoTabPanel()
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
        tabCount--
        UiManager.closeView(tab)
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
