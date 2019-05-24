package org.ro.handler

class DefaultHandler : BaseHandler(), IResponseHandler {

    override fun canHandle(jsonStr: String): Boolean {
        return true
    }

    override fun doHandle() {
        console.log("[DefaultHandler.doHandle] no handler for $logEntry")
    }

}