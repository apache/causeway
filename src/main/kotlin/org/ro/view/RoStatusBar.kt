package org.ro.view

import org.ro.core.event.LogEntry
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
        navbar("nix", type = NavbarType.FIXEDBOTTOM) {
            add(bar)
            urlLabel.content = "defaultUrl"
            userLabel.content = "defaultUser"
        }
    }

    fun updateUser(user: String) {
        userLabel.content = user
    }

    fun update(le: LogEntry?) {
        urlLabel.content = le?.url
    }

}
