package org.ro.view

import org.ro.core.event.LogEntry
import pl.treksoft.kvision.core.CssSize
import pl.treksoft.kvision.core.UNIT
import pl.treksoft.kvision.html.Label
import pl.treksoft.kvision.navbar.Navbar
import pl.treksoft.kvision.navbar.NavbarType
import pl.treksoft.kvision.panel.FlexJustify
import pl.treksoft.kvision.panel.HPanel

class RoStatusBar : Navbar() {

    private var urlLabel = Label()
    private var userLabel = Label()
    private var bar = HPanel(justify = FlexJustify.SPACEBETWEEN) {
        add(urlLabel)
        add(userLabel)
    }

    init {
        navbar(type = NavbarType.FIXEDBOTTOM) {
            height = CssSize(8, UNIT.mm)
            minHeight = CssSize(8, UNIT.mm)
            add(bar)
            urlLabel.content = ""
            userLabel.content = ""
        }
    }

    fun updateUser(user: String) {
        userLabel.content = user
    }

    fun update(le: LogEntry?) {
        urlLabel.content = le?.url
  //      navbar().label  = le?.url 
    }

}
