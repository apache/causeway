package org.ro.handler

import org.ro.layout.Layout

class LayoutHandler : AbstractHandler(), IResponseHandler {

    override fun canHandle(jsonStr: String): Boolean {
       // val rows = //jsonStr["row"].jsonArray
       // return (!rows.isEmpty() && rows.size > 0)
        return false
    }

    override fun doHandle(jsonStr: String) {
        val layout = Layout()
        logEntry.obj = layout
    }

}
