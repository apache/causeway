package org.ro.ui.kv

import org.ro.ui.IconManager
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
        tabPanel.marginTop = CssSize(48, UNIT.px)
    }

    fun addTab(
            title: String,
            panel: Component) {

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

    fun updatePowered(by: String) {
        val color = Col.GOLD
        val bg = Background(
                //image = "fa fa-cube"
                color = color
        )
        tabPanel.background = bg
    }

    fun display(dialog: RoDialog) {
        tabPanel.add(dialog)
        dialog.verticalAlign = VerticalAlign.MIDDLE
        dialog.focus()
    }
    fun remove(dialog: RoDialog) {
        tabPanel.remove(dialog)
    }

}
