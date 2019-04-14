package org.ro.handler


class DefaultHandler : AbstractHandler(), IResponseHandler {

    override fun canHandle(jsonStr: String): Boolean {
        return true
    }

    override fun doHandle() {
        val url = logEntry.url
        console.log("[DefaultHandler.doHandle: $url]")
    }

}