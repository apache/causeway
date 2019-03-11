package org.ro.handler

import kotlinx.serialization.json.JSON
import org.ro.core.event.ListObserver
import org.ro.to.ResultList

class ListHandler : AbstractHandler(), IResponseHandler {

    //TODO structure of json is changed >= 16.2
    override fun canHandle(jsonStr: String): Boolean {
        var answer = false
        try {
            parse(jsonStr)
            answer= true
        } catch (ex: Exception) {
        }
        return answer
    }

    override fun doHandle(jsonStr: String) {
        val resultList = parse(jsonStr)
        logEntry.obj = resultList
        val lo: ListObserver = logEntry.initListObserver()
        lo.update(logEntry)
        val members = resultList.result!!.value
        for (l in members) {
            l.invoke()
        }
    }

    fun parse(jsonStr: String): ResultList {
        return JSON.parse(ResultList.serializer(), jsonStr)
    }
}