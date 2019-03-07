package org.ro.core.event

import org.ro.to.Link

class Node(private var entry:LogEntry) {
    private var children: MutableList<Any>? = mutableListOf()
    private var label: String? = null

    init {
        this.label = this.entry.url
    }

    fun addChild(n: Node) {
        children!!.add(n)
    }

    fun getParentUrl(): String? {
        var result: String? = null
        if (entry.hasResponse()) {
            val response = entry.obj
            val link = response as Link
           //TODO result = link.
        }
        return result
    }

}