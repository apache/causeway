package org.ro.handler


class DefaultHandler : BaseHandler(), IResponseHandler {

    override fun canHandle(jsonStr: String): Boolean {
        return true
    }

    override fun doHandle() {
        val url = logEntry.url
        val response = logEntry.response
        val obs = logEntry.observer;
        console.log("[DefaultHandler.doHandle] no handler for\n" +
                "url:$url \n" +
                "observer:$obs \n" +
                "response:$response")
    }

}