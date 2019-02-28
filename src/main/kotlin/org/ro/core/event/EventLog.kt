package org.ro.core.event

import org.ro.Application
import org.ro.handler.Dispatcher
import kotlin.js.Date

/**
 * Keeps a log of remote invocations and the responses.
 * Subsequent invocations are served from this cache.
 * UI events (Dialogs, Windows, etc.) are logged here as well.
 *
 * @See https://en.wikipedia.org/wiki/Proxy_pattern
 */
//TODO all invocations should go here in the first place 

object EventLog {
    var log = mutableListOf<LogEntry>()

    fun start(url: String, method: String, body: String = "", obs: ILogEventObserver? = null): LogEntry {
        val entry = LogEntry(url, method, body)
        entry.observer = obs
        this.log.add(entry)
        updateStatus(entry)
        return entry
    }

    fun add(description: String) {
        val entry = LogEntry(description)
        entry.createdAt = Date()
        this.log.add(entry)
        updateStatus(entry)
    }

    fun update(description: String): LogEntry? {
        val entry = this.find(description)
        entry!!.updatedAt = Date()
        return entry
    }

    fun close(url: String) {
        val entry = this.findView(url)
        if (null == entry) {
            // Happens with 'Log Entries (x)'
        } else {
            entry.setClose()
            updateStatus(entry)
        }
    }

    fun end(url: String, response: String): LogEntry? {
        val entry: LogEntry? = this.find(url)
        if (entry != null) {
            entry.setSuccess(response)
            updateStatus(entry)
        }
        return entry
    }

    fun fault(url: String, fault: String) {
        val entry: LogEntry? = this.find(url)
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
        if (isUrl(url)) {
            if (isRedundant(url)) {
                return findSimilar(url)
            } else {
                return findExact(url)
            }
        } else {
 //           console.log("[$url isUrl=false]")
            return findView(url)
        }
    }

    private fun isRedundant(url: String): Boolean {
        return (urlContains(url, "object-layout") || urlContains(url, "/properties/"))
    }

    private fun urlContains(url: String, search : String): Boolean {
        val index = url.indexOf(search)
        val answer = index >= 0
 //       console.log("[$url contains $search=$answer]")
        return answer
    }

    private fun isUrl(url: String): Boolean {
        return url.startsWith("http")
    }

    internal fun findExact(url: String): LogEntry? {
        for (le in this.log) {
            // assumes urls are unique !
            if (le.url == url) {
 //               console.log("[$url foundExact=true]")
                return le
            }
        }
//        console.log("[$url foundExact=false]")
        return null
    }

    internal fun findView(url: String): LogEntry? {
        for (le in log) {
            if ((le.url == url) && (le.isView())) {
 //               console.log("[$url view=true]")
                return le
            }
        }
//        console.log("[$url view=false]")
        return null
    }

    internal fun findSimilar(url: String): LogEntry? {
        val argArray: List<String> = url.split("/")
        for (le in log) {
            val idxArray: List<String> = le.url.split("/")
            if (areSimilar(argArray, idxArray)) {
//                console.log("[$url foundSimilar=true]")
                return le
            }
        }
//        console.log("[$url foundSimilar=false]")
        return null

    }

    private fun areSimilar(argArray: List<String>, idxArray: List<String>, allowedDiff: Int = 1): Boolean {
        if (argArray.size != idxArray.size) {
            return false
        }
        var diffCnt = 0

        var i = 0
        for (ai: String in argArray) {
            if (ai != idxArray[i]) {
                diffCnt++
                val n = ai.toIntOrNull()
                // if the difference is a String, it is not allowed and counts double
                if (n != null) {
                    diffCnt++
                }
            }
            i++
        }
        return diffCnt <= allowedDiff
    }

    fun getLogStartTime(): Int? {
        val first = this.log[0]
        return first.start
    }

    fun isCached(url: String): Boolean {
        val le = this.find(url)
        if ((le != null) && (le.hasResponse() || le.isView())) {
            le.retrieveResponse()
            Dispatcher.handle(le)
            return true
        }
        return false
    }

}