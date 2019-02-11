package org.ro.core.event

import kotlinx.serialization.ImplicitReflectionSerializer

/**
 * @see: https://en.wikipedia.org/wiki/Observer_pattern
 *
 * In the original pattern the relation Observable:Observer 1:n,
 * Here it is n:1 (LogEvent:Observer).
 *
 * Observer is:
 * @item notified about changes to related LogEvents and
 * @item decides when it is time to perform a certain operation,
 * i.e. create a view for an object / a list of objects.
 */
@ImplicitReflectionSerializer
interface ILogEventObserver {
    fun update(le: LogEntry)
}

