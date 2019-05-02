package org.ro.core.event

import kotlinx.serialization.Serializable
import org.ro.core.Menu
import org.ro.core.UiManager
import org.ro.to.Service

@Serializable
class NavigationObserver() : IObserver {

    override fun update(logEntry: LogEntry) {
        val service = logEntry.getObj() as Service
        Menu.add(service)

        if (Menu.isFull()) {
            console.log("[NavigationObserver.update] isFull: ${logEntry.url}")
            //FIXME there is an excess of 3/4 items. Why  
            UiManager.amendMenu()
        }
    }

}