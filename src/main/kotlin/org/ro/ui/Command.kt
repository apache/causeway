package org.ro.ui

import org.ro.core.event.RoXmlHttpRequest
import org.ro.to.Link

interface Command {
    fun execute() {
        // subclass responsibility
    }

    fun invoke(link: Link) {
        RoXmlHttpRequest().invoke(link, null)
    }

}
