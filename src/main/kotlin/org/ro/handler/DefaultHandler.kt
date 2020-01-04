package org.ro.handler

class DefaultHandler : BaseHandler(), IResponseHandler {

    override fun canHandle(response: String): Boolean {
        return true
    }

    override fun doHandle() {
        console.log("[DefaultHandler.doHandle]")
        console.log(logEntry)
        update()
    }

}
