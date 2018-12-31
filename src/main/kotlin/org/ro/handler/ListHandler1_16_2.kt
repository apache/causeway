package org.ro.handler

import kotlinx.serialization.json.JsonObject

class ListHandler1_16_2 : AbstractHandler(), IResponseHandler {

    override fun canHandle(jsonObj: JsonObject): Boolean {
        //FIXME
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

    override fun doHandle(jsonObj: JsonObject): Unit {
//FIXME
/*
var list = List(jsonObj)
var members = list.v
var size: Int = members.size
var objectList: ObjectList = ObjectList(size)
Globals.setList(objectList)
for (l in members) {
    l.invoke()
}
*/
    }

}

