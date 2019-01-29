package org.ro.handler

import org.ro.core.event.ListObserver
import org.ro.to.List

class ListHandler : AbstractHandler(), IResponseHandler {

    override fun canHandle(jsonStr: String): Boolean {
     /*   val r = jsonObj["result"].jsonObject
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
        return list.size > 0     */
        return false;
    }

    fun canHandle_16_2(jsonStr: String): Boolean {
        //TODO structure of json is changed >= 16.2
        /*
        var v: Object = jsonObj.value
        if ((v == null) || isEmptyObject(v)) {
            return false
        }
        if (v is Array) {
            var va: Array = v as Array
            return va.length > 0
        } 
        */
        return false
    }


    override fun doHandle(jsonStr: String) {
        val list = List()
        logEntry.obj = list
        val lo: ListObserver = logEntry.initListObserver()
        lo.update(logEntry)
        val members = list.getResult()!!.valueList
        for (l in members) {
            l.invoke(lo)
        }
    }

}