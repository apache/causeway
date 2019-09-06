package org.ro.org.ro.view

import pl.treksoft.kvision.core.CssSize
import pl.treksoft.kvision.core.PopoverOptions
import pl.treksoft.kvision.core.TooltipOptions
import pl.treksoft.kvision.core.UNIT
import pl.treksoft.kvision.html.Image.Companion.image
import pl.treksoft.kvision.panel.VPanel
import pl.treksoft.kvision.require
import pl.treksoft.kvision.utils.px

class BasicTab : VPanel() {
    init {
        this.marginTop = 10.px
        this.minHeight = 400.px
        vPanel(spacing = 3) {
            image(require("img/kroviz-logo.svg")) {
                width = CssSize(200, UNIT.px)
                height = CssSize(100, UNIT.px)
                enableTooltip(TooltipOptions(title = ("This is a tooltip")))
                enablePopover(
                        PopoverOptions(
                                title = ("This is a popover"),
                                content = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Fusce nec fringilla turpis."
                        )
                )
            }
        }
        vPanel(spacing = 3) {
            image(require("img/uml-overview.png")) {
                width = CssSize(1200, UNIT.px)
                height = CssSize(800, UNIT.px)
                enableTooltip(TooltipOptions(title = ("This is a tooltip")))
                enablePopover(
                        PopoverOptions(
                                title = ("This is a popover"),
                                content = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Fusce nec fringilla turpis."
                        )
                )
            }
        }

    }
}

