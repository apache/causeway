package org.ro.handler

import kotlinx.serialization.json.JSON
import org.ro.core.event.ListObserver
import org.ro.to.ResultList

class ResultListHandler : AbstractHandler(), IResponseHandler {

    //TODO structure of json is changed >= 16.2
    override fun canHandle(jsonStr: String): Boolean {
        var answer = false
        try {
            val obj = parse(jsonStr)
            logEntry.setObj(obj)
            answer= true
        } catch (ex: Exception) {
        }
        return answer
    }

    override fun doHandle() {
        val obs: ListObserver = ListObserver()
        logEntry.observer = obs
        obs.update(logEntry)

        val resultList = logEntry.getObj() as ResultList
        val members = resultList.result!!.value
        for (l in members) {
            l.invoke(obs)
        }
    }

    fun parse(jsonStr: String): ResultList {
        return JSON.parse(ResultList.serializer(), jsonStr)
    }
}