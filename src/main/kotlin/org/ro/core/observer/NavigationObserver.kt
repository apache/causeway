package org.ro.core.event

import kotlinx.serialization.Serializable
import org.ro.core.Menu
import org.ro.core.TransferObject
import org.ro.core.UiManager
import org.ro.core.observer.BaseObserver
import org.ro.to.Result
import org.ro.to.Service

@Serializable
class NavigationObserver : BaseObserver() {
    private var isRendered = false;

    override fun update(logEntry: LogEntry) {
        val obj = logEntry.getObj()

        when (obj) {
            is Result -> handleResult(obj)
            is Service -> Menu.add(obj)
            else -> log(logEntry)
        }

        if (Menu.isFull()) {
            if (isRendered) {
                //FIXME there is an excess of 3/4 items. Why  
                console.log("[NavigationObserver.update] Menu.isFull: ${logEntry.url}")
            } else {
                UiManager.amendMenu()
                isRendered = true
            }
        }
    }
    
    private fun handleResult(obj: TransferObject) {
        val result = obj as Result
        val values = result.value
        Menu.limit = values.size
        for (l in values) {
            l.invoke(this)
        }
    }

}