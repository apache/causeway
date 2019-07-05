package org.ro.view

import org.ro.core.event.RoXmlHttpRequest
import org.ro.to.Link

interface Command {
    fun execute()

    fun invoke(link: Link) {
        RoXmlHttpRequest().invoke(link, null)
    }

}
