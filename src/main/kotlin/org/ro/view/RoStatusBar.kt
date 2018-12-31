package org.ro.view

import org.ro.core.event.LogEntry
import pl.treksoft.kvision.html.Label

class RoStatusBar {
    //private var statusIcon: LinkButton = LinkButton()
    var url: Label = Label()
    var duration: Label = Label()
    //private var spacer:Spacer =  Spacer()
    var user: Label = Label()
        get

    fun update(le: LogEntry?) {

    }

}
