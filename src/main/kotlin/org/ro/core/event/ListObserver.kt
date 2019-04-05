package org.ro.core.event

import org.ro.core.DisplayManager
import org.ro.core.model.ObjectAdapter
import org.ro.core.model.ObjectList
import org.ro.handler.TObjectHandler
import org.ro.layout.Layout
import org.ro.to.ResultList
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
    var list = ObjectList()

    // Handlers should set object into le after successful parsing
    override fun update(le: LogEntry) {
        val obj = le.obj
        val url = le.url

        when (obj) {
            is ResultList -> list.initSize(obj.result!!.valueList().size)
            is TObject -> handleObject(url, le)
            is Layout -> list.setLayout(obj)
            else -> console.log("[ListObserver.update] unexpected:\n $obj]")
        }
        
        //TODO are list & layout the only criteria?
        if (list.hasLayout() && list.isFull()) {
            handleView(url)
        }
    }

    private fun handleObject(url: String, le: LogEntry) {
        if (list.isFull()) {
            console.log("[ListObserver.handleObject full, not adding: $url")
        } else {
            //TODO eventually set/get LogEntry.tObject
            val jsonStr = le.response
            val tObj = TObjectHandler().parse(jsonStr)
            loadLayout(tObj)
            val oa = ObjectAdapter(tObj)
            list.add(oa)
        }
    }

    private fun handleView(url: String) {
        val le2 = EventLog.find(url)
        val b = (le2 != null) && (le2.isView())
        if (b) {
            console.log("View already opened: $url")
        } else {
            //TODO on runFixtureScript this is passed multiple times (but no view opened (which is correct))
            DisplayManager.addView(list) //open
        }
    }

    private fun loadLayout(tObject: TObject) {
        val link = tObject.getLayoutLink()
        val href = link!!.href
        val le = EventLog.find(href)
        if (le == null) {
            link.invoke(this)
        }
    }

}