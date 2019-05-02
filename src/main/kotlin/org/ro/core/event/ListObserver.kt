package org.ro.core.event

import kotlinx.serialization.Serializable
import org.ro.core.UiManager
import org.ro.core.model.ObjectAdapter
import org.ro.core.model.ObjectList
import org.ro.layout.Layout
import org.ro.to.Property
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
@Serializable
class ListObserver : IObserver {
    var list = ObjectList()

    // Handlers should set object into le after successful parsing
    override fun update(le: LogEntry) {
        val obj = le.getObj()

        when (obj) {
            is ResultList -> handleList(obj)
            is TObject -> handleObject(le)
            is Layout -> list.layout = obj
            is Property -> handleProperty(obj)
            else -> log(le)
        }

        if (list.hasLayout()) {
            handleView()
        }
    }

    private fun log(le:LogEntry) {
        console.log("[ListObserver.update] unexpected:\n ${le.toString()}")
    }

    private fun handleList(resultList: ResultList) {
        console.log("[ListObserver.update] obj == ResultList")
    }
    
    private fun handleProperty(p: Property) {
        //TODO differentiate between Property and PropertyDescription
        val ext = p.extensions!!
        if (ext.friendlyName.isNotEmpty()) {
            console.log("[ListObserver.handleProperty] -> description")
        } else {
            console.log("[ListObserver.handleProperty]")
        }
    }

    private fun handleObject(le: LogEntry) {
        // FIXME eventually this is called multiple times, which may be wrong
        console.log("[ListObserver.handleObject] adding: ${le.url}")
        val tObj = le.getObj() as TObject
        loadLayout(tObj)
        val oa = ObjectAdapter(tObj)
        list.add(oa)
        console.log("[ListObserver.handleObject] objects in list: ${list.list.size}")
    }

    private fun handleView() {
        val title: String = "ListObserver"
        console.log("[ListObserver.handleView] about to open: $title")
        val le = EventStore.findView(title)
        val b = le != null
        if (b) {
            console.log("[ListObserver.handleView] already opened: $title")
        } else {
            //TODO on runFixtureScript this is passed multiple times (but no view opened (which is correct))
            UiManager.addView(list) //open
        }
    }

    private fun loadLayout(tObject: TObject) {
        val link = tObject.getLayoutLink()
        val href = link!!.href
        val le = EventStore.find(href)
        if (le == null) {
            link.invoke(this)
        }
    }

}