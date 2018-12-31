package org.ro.handler

import kotlinx.serialization.json.JsonObject
import org.ro.core.event.ListObserver
import org.ro.to.List

class ListHandler : AbstractHandler(), IResponseHandler {

    override fun canHandle(jsonObj: JsonObject): Boolean {
        val r = jsonObj["result"].jsonObject
        if (r.isEmpty()) {
            return false
        }
        // Prototyping#openRestApi
        val value = jsonObj["value"]
        if (value.toString() == "http:/restful/") {
            return false
        }
        val list = value.jsonArray
        if (list.isEmpty()) {
            return false
        }
        return list.size > 0
    }

    override fun doHandle(jsonObj: JsonObject) {
        val list = List(jsonObj)
        logEntry.obj = list
        val lo: ListObserver = logEntry.initListObserver()
        lo.update(logEntry)
        val members = list.getResult()!!.valueList
        for (l in members) {
            l.invoke(lo)
        }
    }

}