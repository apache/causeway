package org.ro.core.event

import org.ro.core.UiManager
import org.ro.core.aggregator.Aggregator
import org.ro.handler.ResponseHandler
import pl.treksoft.kvision.utils.observableListOf

/**
 * Keeps a log of remote invocations and the responses.
 * Subsequent invocations are served from this cache.
 * UI events (Dialogs, Windows, etc.) are logged here as well.
 *
 * @See https://en.wikipedia.org/wiki/Proxy_pattern
 * @See https://martinfowler.com/eaaDev/EventSourcing.html
 */
object EventStore {
    var log = observableListOf<LogEntry>()
    var logStartTime: Int = 0

    private fun log(logEntry: LogEntry) {
        log.add(logEntry)
        if (log.size == 1) {
            logStartTime = logEntry.createdAt.getMilliseconds()
        }
    }

    fun start(url: String, method: String, body: String = "", aggregator: Aggregator? = null): LogEntry {
        val entry = LogEntry(url, method, body)
        entry.aggregator = aggregator
        log(entry)
        updateStatus(entry)
        return entry
    }

    fun add(url: String) {
        val entry = LogEntry(url = url)
        log(entry)
        updateStatus(entry)
    }

    fun addView(title: String) {
        val entry = LogEntry(title = title)
        log(entry)
        updateStatus(entry)
    }

    fun update(description: String): LogEntry? {
        val entry = find(description)
        entry!!.setSuccess()
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
            entry.response = response
            entry.setSuccess()
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
        UiManager.updateStatus(entry)
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
        val sl = removeHexCode(searchUrl)
        val cl = removeHexCode(compareUrl)
        val searchList: List<String> = sl.split("/")
        val compareList: List<String> = cl.split("/")
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

    //TODO C&P from LogEntry.removeHexCode -> apply extract refactoring
    private fun removeHexCode(input: String): String {
        var output = ""
        val list: List<String> = input.split("/")
        //split string by "/" and remove parts longer than 40chars
        for (s in list) {
            output += "/"
            output += if (s.length < 40) {
                s
            } else {
                "..."
            }
        }
        return output
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
