package org.ro.core.event

import org.ro.core.DisplayManager
import org.ro.core.Globals
import org.ro.core.Utils
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
    private var log = mutableListOf<LogEntry>()

    fun start(url: String, method: String, body: String? = null, obs: ILogEventObserver? = null): LogEntry {
        val entry = LogEntry(url, method, body)
        entry.observer = obs
        this.log.add(entry)
        DisplayManager.updateStatus(entry)
        return entry
    }

    fun add(description: String) {
        val entry = LogEntry(description)
        entry.createdAt = Date()
        this.log.add(entry)
        DisplayManager.updateStatus(entry)
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
            DisplayManager.updateStatus(entry)
        }
    }

    fun end(url: String, response: String): LogEntry? {
        val entry: LogEntry? = this.find(url)
        entry!!.setSuccess(response)
        DisplayManager.updateStatus(entry)
        return entry
    }

    fun fault(url: String, fault: String) {
        val entry: LogEntry? = this.find(url)
        entry!!.setError(fault)
        DisplayManager.updateStatus(entry)
    }

    /**
     * Answers the first matching entry.
     * @param url
     * @return
     */
    fun find(url: String): LogEntry? {
        val le: LogEntry?
        if (!this.isViewUrl(url)) {
            le = if (Utils().endsWith(url, "object-layout") ||
                    url.indexOf("/properties/") > 0) {
                this.findSimilar(url)
            } else {
                this.findExact(url)
            }
        } else {
            le = this.findView(url)
        }
        return le
    }

    private fun isViewUrl(url: String): Boolean {
        return (url.indexOf("http") < 0)
    }

    internal fun findExact(url: String): LogEntry? {
        for (le in this.log) {
            // assumes urls are unique !
            if (le.url == url) {
                console.log("[foundExact] $url")
                return le
            }
        }
        console.log("[NOT foundExact] $url")
        return null
    }

    internal fun findView(url: String): LogEntry? {
        if (this.isViewUrl(url)) {
            for (le in this.log) {
                if ((le.url == url) && (le.isView())) {
                    console.log("[foundView] $url")
                    return le
                }
            }
        }
        console.log("[NOT foundView] $url")
        return null
    }

    internal fun findSimilar(url: String): LogEntry? {
        val argArray: List<String> = url.split("/")
        for (le in this.log) {
            val idxArray: List<String> = le.url.split("/")
            if (this.areSimilar(argArray, idxArray)) {
                console.log("[foundSimilar] $url")
                return le
            }
        }
        console.log("[NOT foundSimilar] $url")
        return null

    }

    private fun areSimilar(argArray: List<String>, idxArray: List<String>, allowedDiff: Int = 1): Boolean {
        if (argArray.size != idxArray.size) {
            return false
        }
        var diffCnt = 0
        var len = argArray.size
        var ai: String
        var n: Number
        var isString: Boolean
/* FIXME        
for (i: Int i <= len i++) {
            ai = argArray[i]
            if (ai != idxArray[i]) {
                diffCnt += 1
                n = Number(ai)
                isString = isNaN(n)
                // if the difference is a String, it is not allowed and counts double
                if (isString) {
                    diffCnt += 1
                }
            } 
        } */
        return diffCnt <= allowedDiff
    }

    fun getEntries(): MutableList<LogEntry>? {
        return this.log
    }

    fun getLogStartTime(): Int? {
        val first = this.log[0]
        return first.start
    }

    fun isCached(url: String): Boolean {
        val le = this.find(url)
        if ((le != null) && (le.hasResponse() || le.isView())) {
            le.retrieveResponse()
            Globals.dispatcher.handle(le)
            return true
        }
        return false
    }

}