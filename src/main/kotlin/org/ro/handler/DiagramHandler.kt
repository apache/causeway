package org.ro.handler

class DiagramHandler : BaseHandler(), IResponseHandler {

    override fun doHandle() {
        update()
    }

    override fun canHandle(response: String): Boolean {
        return response.startsWith("<") && response.endsWith(">")
    }


}
