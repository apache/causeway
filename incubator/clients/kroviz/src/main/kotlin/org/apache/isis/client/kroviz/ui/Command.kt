package org.apache.isis.client.kroviz.ui

import org.apache.isis.client.kroviz.core.event.RoXmlHttpRequest
import org.apache.isis.client.kroviz.to.Link

interface Command {
    fun execute() {
        // subclass responsibility
    }

    fun invoke(link: Link) {
        RoXmlHttpRequest().invoke(link, null)
    }

}
