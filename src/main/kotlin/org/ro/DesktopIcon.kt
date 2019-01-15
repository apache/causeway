/*
 * Copyright (c) 2018. Robert Jaros
 */

package org.ro

import pl.treksoft.kvision.core.WhiteSpace
import pl.treksoft.kvision.html.Icon
import pl.treksoft.kvision.html.Label
import pl.treksoft.kvision.panel.FlexAlignItems
import pl.treksoft.kvision.panel.VPanel
import pl.treksoft.kvision.utils.px

class DesktopIcon(icon: String, content: String) : VPanel(alignItems = FlexAlignItems.CENTER) {
    init {
        width = 64.px
        height = 64.px
        add(Icon(icon).addCssClass("fa-3x"))
        add(Label(content).apply {
            whiteSpace = WhiteSpace.NOWRAP
        })
    }
}
