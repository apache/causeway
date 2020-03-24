package org.apache.isis.client.kroviz.handler

class DefaultHandler : BaseHandler() {

    override fun canHandle(response: String): Boolean {
        return true
    }

    override fun doHandle() {
        console.log("[DefaultHandler.doHandle]")
        console.log(logEntry)
        update()
    }

}
