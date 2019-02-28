package org.ro.core.event

import org.ro.core.Utils

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
            val response: String = entry.getResponse()
            result = Utils().getUpHref(response)
        }
        return result
    }

}