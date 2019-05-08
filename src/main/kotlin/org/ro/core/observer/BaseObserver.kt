package org.ro.core.observer

import org.ro.core.event.IObserver
import org.ro.core.event.LogEntry

abstract class BaseObserver : IObserver {
    protected fun log(le: LogEntry) {
        console.log("[ListObserver.update] unexpected:\n ${le.toString()}")
    }

}