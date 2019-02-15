package org.ro.core.event

import kotlinx.serialization.ImplicitReflectionSerializer
import org.ro.core.DisplayManager
import org.ro.core.model.ObjectAdapter
import org.ro.core.model.ObjectList
import org.ro.handler.TObjectHandler
import org.ro.layout.Layout
import org.ro.to.Invokeable
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
@ImplicitReflectionSerializer
class ListObserver : ILogEventObserver {
    var list: ObjectList = ObjectList()

    /* test scope only */
    fun getList(): ObjectList {
        return list
    }

    //TODO rework: method too complex, uses JsonObj (which is not available anymore)
    // Handlers should set object into le after successful parsing
    override fun update(le: LogEntry) {
        val obj =  le.obj
        val url = le.url

        if (obj is ObjectList) {
            list.initSize(obj.length())
        }
        if (obj is TObject) {
            if (list.isFull()) {
                console.log("List full, not adding: $url")
            } else {
                //TODO eventually set/get LogEntry.tObject
                val jsonStr: String = le.response
                val tObj = TObjectHandler().parse(jsonStr)
                loadLayout(tObj)
                val oa = ObjectAdapter(tObj)
                list.add(oa)
            }
        }

        if (obj is Layout) {
            //TODO if le.tObject is already set it should contain Layout
            val l = Layout("no debug info")
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
    
    private fun loadLayout(tObject: TObject) {
        val link: Link? = tObject.getLayoutLink()
        val href: String = link!!.href
        var le: LogEntry? = null
        if (href != null) {
            le = EventLog.find(href)
        }
        if (le == null) {
            val i = Invokeable(href)
            i.invoke(this)
        }
    }

}