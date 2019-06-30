package org.ro.core.event

import com.lightningkite.kotlin.observable.list.observableListOf
import org.ro.Application
import org.ro.handler.ResponseHandler
import kotlin.js.Date

/**
 * Keeps a log of remote invocations and the responses.
 * Subsequent invocations are served from this cache.
 * UI events (Dialogs, Windows, etc.) are logged here as well.
 *
 * @See https://en.wikipedia.org/wiki/Proxy_pattern
 */
object EventStore {
    var log = observableListOf<LogEntry>()

    fun start(url: String, method: String, body: String = "", obs: IObserver? = null): LogEntry {
        val entry = LogEntry(url, method, body)
        entry.observer = obs
        log.add(entry)
        updateStatus(entry)
        return entry
    }

    fun add(url: String) {
        val entry = LogEntry(url = url)
        entry.createdAt = Date()
        log.add(entry)
        updateStatus(entry)
    }

    fun addView(title: String) {
        val entry = LogEntry(title = title)
        entry.createdAt = Date()
        log.add(entry)
        updateStatus(entry)
    }

    fun update(description: String): LogEntry? {
        val entry = find(description)
        entry!!.updatedAt = Date()
        return entry
    }

    fun close(url: String) {
        val entry = findView(url)
        if (null == entry) {
            // Happens with 'Log Entries (x)'
        } else {
            entry.setClose()
            updateStatus(entry)
        }
    }

    fun end(url: String, response: String): LogEntry? {
        val entry: LogEntry? = find(url)
        if (entry != null) {
            entry.setSuccess(response)
            updateStatus(entry)
        }
        return entry
    }

    fun fault(url: String, fault: String) {
        val entry: LogEntry? = find(url)
        entry!!.setError(fault)
        updateStatus(entry)
    }

    private fun updateStatus(entry: LogEntry) {
        Application.statusBar.update(entry)
    }

    /**
     * Answers the first matching entry.
     * @param url
     * @return
     */
    fun find(url: String): LogEntry? {
        val isRedundant = urlContains(url, "object-layout") || urlContains(url, "/properties/")
        if (isRedundant) {
            return findEquivalent(url)
        } else {
            return findExact(url)
        }
    }

    private fun urlContains(url: String, search: String): Boolean {
        val index = url.indexOf(search)
        val answer = index >= 0
        return answer
    }

    internal fun findExact(url: String): LogEntry? {
        for (le in log) {
            if (le.url == url) {
                return le
            }
        }
        return null
    }

    internal fun findView(title: String): LogEntry? {
        for (le in log) {
            if ((le.title == title) && (le.isView())) {
                return le
            }
        }
        return null
    }

    internal fun findEquivalent(url: String): LogEntry? {
        for (le in log) {
            if (areEquivalent(url, le.url)) {
                return le
            }
        }
        return null
    }

    private fun areEquivalent(searchUrl: String, compareUrl: String, allowedDiff: Int = 1): Boolean {
        val searchList: List<String> = searchUrl.split("/")
        val compareList: List<String> = compareUrl.split("/")
        if (searchList.size != compareList.size) {
            return false
        }

        var diffCnt = 0
        for ((i, s) in searchList.withIndex()) {
            val c = compareList[i];
            if (!s.equals(c)) {
                diffCnt++
                val n = s.toIntOrNull()
                // if the difference is a String, it is not allowed and counts double
                if (n == null) {
                    diffCnt++
                }
            }
        }
        return diffCnt <= allowedDiff
    }

    fun getLogStartTime(): Int? {
        val first = this.log[0]
        return first.start
    }

    //TODO function has a side effect - refactor
    fun isCached(url: String): Boolean {
        val le = this.find(url)
        if ((le != null) && (le.hasResponse() || le.isView())) {
            le.retrieveResponse()
            ResponseHandler.handle(le)
            return true
        }
        return false
    }

    fun reset() {
        log = observableListOf<LogEntry>()
    }

}
