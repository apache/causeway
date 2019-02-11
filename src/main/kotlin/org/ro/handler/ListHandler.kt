package org.ro.handler

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.JSON
import org.ro.core.event.ListObserver
import org.ro.to.Invokeable
import org.ro.to.ResultList

@ImplicitReflectionSerializer
class ListHandler : AbstractHandler(), IResponseHandler {

    //TODO structure of json is changed >= 16.2
    override fun canHandle(jsonStr: String): Boolean {
        try {
            val resultList = JSON.parse(ResultList.serializer(), jsonStr)
            // "resulttype": "list"
            return true
        } catch (ex: Exception) {
            console.log("[ListHandler fails on: $jsonStr]")
            return false
        }
    }

    override fun doHandle(jsonStr: String) {
        val resultList = JSON.parse(ResultList.serializer(), jsonStr)
        logEntry.obj = resultList
        val lo: ListObserver = logEntry.initListObserver()
        lo.update(logEntry)
        val members = resultList.result!!.value
        for (l in members) {
            val i = Invokeable(l.href)
            i.invoke()
        }
    }

}