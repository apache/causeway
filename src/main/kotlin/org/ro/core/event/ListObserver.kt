package org.ro.core.event

import kotlinx.serialization.json.JsonObject
import org.ro.core.DisplayManager
import org.ro.core.Utils
import org.ro.core.model.ObjectAdapter
import org.ro.core.model.ObjectList
import org.ro.layout.Layout
import org.ro.to.Link
import org.ro.to.TObject

/**
 * Observers are initially created in ResponseHandler(s) and assigned to the respective LogEntry.
 * They may be passed on to additional LogEntries, eg. those corresponding to related Object or Layout URLs.
 */
/** sequence of operations:
 * (0) list
 * (1) FR_OBJECT                TObjectHandler -> invoke()
 * (2) FR_OBJECT_LAYOUT         layoutHandler -> invoke(layout.getProperties()[].getLink()) link can be null?
 * (3) FR_OBJECT_PROPERTY       PropertyHandler -> invoke()
 * (4) FR_PROPERTY_DESCRIPTION  PropertyDescriptionHandler
 */
class ListObserver : ILogEventObserver {
    var list: ObjectList = ObjectList()

    /* test scope only */
    fun getList(): ObjectList {
        return list
    }

    override fun update(le: LogEntry) {
        val jsonObj: JsonObject? = Utils().toJsonObject(le.response)
        val url = le.url

        if (isList(jsonObj)) {
            val result = jsonObj!!["result"].jsonObject
            val value = result["value"].jsonArray
            val size: Int = value.size
            list.initSize(size)
        }
        if (isObject(jsonObj)) {
            if (list.isFull()) {
                console.log("List full, not adding: $url")
            } else {
                //TODO eventually set/get LogEntry.tObject
                val jsonStr: String = le.response
                val jso: JsonObject? = Utils().toJsonObject(jsonStr)
                val tObj = TObject(jso)
                loadLayout(tObj)
                val oa = ObjectAdapter(tObj)
                list.add(oa)
            }
        }

        if (isLayout(jsonObj)) {
            //TODO if le.tObject is already set it should contain Layout
            val l = Layout(jsonObj)
            list.setLayout(l)
        }
        //TODO are list & layout the only criteria?
        if (list.hasLayout() && list.isFull()) {
            val le2: LogEntry? = EventLog.find(url)
            val b: Boolean = (le2 != null) && (le2.isView())
            if (b) {
                console.log("View already opened: $url")
            } else {
                //TODO on runFixtureScript this is passed multiple times (but no view opened (which is correct))
                DisplayManager.addView(list) //open
            }
        }
    }

    //TODO eventually move to LogEntry
    private fun isList(jsonObj: JsonObject?): Boolean {
        var b = false
        if (jsonObj!!["resulttype"].toString() == "list") {
            b = true
        }
        if (jsonObj["memberType"].toString() == "collection") {
            b = true
        }
        return b
    }

    private fun isObject(jsonObj: JsonObject?): Boolean {
        val jsonEl = jsonObj!!["instanceId"]
        return !jsonEl.isNull
    }

    private fun isLayout(jsonObj: JsonObject?): Boolean {
        val jsonEl = jsonObj!!["row"] 
        return !jsonEl.isNull
    }

    private fun loadLayout(tObject: TObject) {
        val link: Link? = tObject.getLayoutLink()
        val href: String? = link!!.getHref()
        var le: LogEntry? = null
        if (href != null) {
            le = EventLog.find(href)
        }
        if (le == null) {
            link.invoke(this)
        }
    }

}