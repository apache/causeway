package org.apache.isis.client.kroviz.ui.kv

import org.apache.isis.client.kroviz.utils.IconManager
import pl.treksoft.kvision.core.*
import pl.treksoft.kvision.panel.SimplePanel

/**
 * Area between menu bar at the top and the status bar at the bottom.
 * Contains:
 * @Item TabPanel with Tabs
 */
object RoView {
    val tabPanel = RoTabPanel()
    private var tabCount = 0

    init {
        tabPanel.marginTop = CssSize(40, UNIT.px)
    }

    fun addTab(
            title: String,
            panel: Component) {
        panel.addBsBorder(BsBorder.BORDER)
        val index = tabPanel.findTab(title)
        if (index != null) {
            val tab = tabPanel.getChildComponent(index) as SimplePanel
            removeTab(tab)
            tabPanel.removeTab(index)
        }

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

    fun removeTab(tab: SimplePanel) {
        tabCount--
        UiManager.closeView(tab)
    }

    fun findActive(): SimplePanel? {
        val index = tabPanel.activeIndex
        if (index > 0) {
            val tab = tabPanel.getChildComponent(index) as SimplePanel
            return (tab)
        }
        return null
    }

}
