package org.ro.core.event

import org.ro.core.DisplayManager
import org.ro.core.Menu

class NavigationObserver(var baseUrl: String) : ILogEventObserver {

    override fun update(le: LogEntry) {
        if (Menu.isFull()) {
            console.log("[NavigationObserver.update] isFull: ${le.url}")
            //FIXME there is an excess of 3/4 items. Why  
            DisplayManager.amendMenu()
        }
    }

}