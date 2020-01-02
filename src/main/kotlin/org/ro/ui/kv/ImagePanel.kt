package org.ro.ui.kv

import pl.treksoft.kvision.core.CssSize
import pl.treksoft.kvision.core.PopoverOptions
import pl.treksoft.kvision.core.TooltipOptions
import pl.treksoft.kvision.core.UNIT
import pl.treksoft.kvision.html.image
import pl.treksoft.kvision.panel.VPanel
import pl.treksoft.kvision.panel.vPanel
import pl.treksoft.kvision.require
import pl.treksoft.kvision.utils.px

class ImagePanel : VPanel() {
    init {
        this.marginTop = 10.px
        this.minHeight = 400.px
        vPanel(spacing = 3) {
            image(require("img/plantUml_stateSample.svg")) {
                width = CssSize(1600, UNIT.px)
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

