package com.example

import pl.treksoft.kvision.core.Background
import pl.treksoft.kvision.core.Col
import pl.treksoft.kvision.core.Container
import pl.treksoft.kvision.dropdown.DropDown.Companion.dropDown
import pl.treksoft.kvision.html.TAG
import pl.treksoft.kvision.html.Tag
import pl.treksoft.kvision.html.Tag.Companion.tag
import pl.treksoft.kvision.i18n.I18n.tr
import pl.treksoft.kvision.panel.Direction
import pl.treksoft.kvision.panel.SideTabSize
import pl.treksoft.kvision.panel.SimplePanel
import pl.treksoft.kvision.panel.SplitPanel.Companion.splitPanel
import pl.treksoft.kvision.panel.StackPanel.Companion.stackPanel
import pl.treksoft.kvision.panel.TabPanel.Companion.tabPanel
import pl.treksoft.kvision.panel.TabPosition
import pl.treksoft.kvision.panel.VPanel.Companion.vPanel
import pl.treksoft.kvision.utils.px

class ContainersTab : SimplePanel() {
    init {
        this.marginTop = 10.px
        vPanel(spacing = 5) {
            addStackPanel()
            addTabPanel()
            addTabPanelLeft()
            addTabPanelRight()
            addVerticalSplitPanel()
            addHorizontalSplitPanel()
        }
    }

    private fun Container.addStackPanel() {
        tag(TAG.H4, tr("Stack panel"))
        stackPanel {
            add(Tag(TAG.DIV, "&nbsp;", rich = true) {
                background = Background(Col.BLUE)
                height = 40.px
            }, "/containers/blue")
            add(Tag(TAG.DIV, "&nbsp;", rich = true) {
                background = Background(Col.GREEN)
                height = 40.px
            }, "/containers/green")
        }
        dropDown(
            tr("Activate panel from the stack"), listOf(
                tr("Blue panel") to "#!/containers/blue",
                tr("Green panel") to "#!/containers/green"
            )
        )
    }

    private fun Container.addTabPanel() {
        tag(TAG.H4, tr("Tab panel"))
        tabPanel {
            addTab(tr("Blue panel"), Tag(TAG.DIV, "&nbsp;", rich = true) {
                background = Background(Col.BLUE)
                height = 40.px
            })
            addTab(tr("Green panel"), Tag(TAG.DIV, "&nbsp;", rich = true) {
                background = Background(Col.GREEN)
                height = 40.px
            })
        }
    }

    private fun Container.addTabPanelLeft() {
        tag(TAG.H4, tr("Tab panel with tabs on the left"))
        tabPanel(TabPosition.LEFT, SideTabSize.SIZE_1) {
            addTab(tr("Blue panel"), Tag(TAG.DIV, "&nbsp;", rich = true) {
                background = Background(Col.BLUE)
                height = 140.px
            })
            addTab(tr("Green panel"), Tag(TAG.DIV, "&nbsp;", rich = true) {
                background = Background(Col.GREEN)
                height = 140.px
            })
        }
    }

    private fun Container.addTabPanelRight() {
        tag(TAG.H4, tr("Tab panel with tabs on the right"))
        tabPanel(TabPosition.RIGHT, SideTabSize.SIZE_1) {
            addTab(tr("Blue panel"), Tag(TAG.DIV, "&nbsp;", rich = true) {
                background = Background(Col.BLUE)
                height = 140.px
            })
            addTab(tr("Green panel"), Tag(TAG.DIV, "&nbsp;", rich = true) {
                background = Background(Col.GREEN)
                height = 140.px
            })
        }
    }

    private fun Container.addVerticalSplitPanel() {
        tag(TAG.H4, tr("Vertical split panel"))
        splitPanel {
            tag(TAG.DIV, "&nbsp;", rich = true) {
                background = Background(Col.BLUE)
                height = 200.px
            }
            tag(TAG.DIV, "&nbsp;", rich = true) {
                background = Background(Col.GREEN)
                height = 200.px
            }
        }
    }

    private fun Container.addHorizontalSplitPanel() {
        tag(TAG.H4, tr("Horizontal split panel"))
        splitPanel(direction = Direction.HORIZONTAL) {
            height = 220.px
            tag(TAG.DIV, "&nbsp;", rich = true) {
                background = Background(Col.BLUE)
                height = 100.px
            }
            tag(TAG.DIV, "&nbsp;", rich = true) {
                background = Background(Col.GREEN)
                height = 100.px
            }
        }
    }
}
