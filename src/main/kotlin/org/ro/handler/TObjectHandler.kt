package org.ro.handler

class TObjectHandler : AbstractHandler(), IResponseHandler {

    override fun canHandle(jsonStr: String): Boolean {
// FIXME       return hasMembers(jsonObj) && !isService(jsonObj)
        return false
    }

    override fun doHandle(jsonStr: String) {
// FIXME       val tObj = TObject(jsonObj)
//        logEntry.obj = tObj
    }

}

