package org.ro.core.event

import kotlinx.serialization.Serializable
import org.ro.core.model.ObjectAdapter
import org.ro.core.model.ObjectList
import org.ro.core.observer.BaseObserver
import org.ro.core.observer.getLayoutLink
import org.ro.layout.Layout
import org.ro.to.*
import org.ro.view.IconManager
import org.ro.view.RoView
import org.ro.view.table.fr.FixtureResultTable

/** sequence of operations:
 * (0) list
 * (1) FR_OBJECT                TObjectHandler -> invoke()
 * (2) FR_OBJECT_LAYOUT         layoutHandler -> invoke(layout.getProperties()[].getLink()) link can be null?
 * (3) FR_OBJECT_PROPERTY       PropertyHandler -> invoke()
 * (4) FR_PROPERTY_DESCRIPTION  PropertyDescriptionHandler
 */
@Serializable
class ListObserver : BaseObserver() {
    var list = ObjectList()
    private var isRendered = false;

    // Handlers should set object into le after successful parsing
    override fun update(le: LogEntry) {
        val obj = le.getObj()

        when (obj) {
            is ResultList -> handleList(obj)
            is TObject -> handleObject(obj)
            is Layout -> list.layout = obj
            is Property -> handleProperty(obj)
            else -> log(le)
        }

        console.log("[ListObserver.handleView] hasLayout: ${list.hasLayout()}")
        if (list.hasLayout()) {
            handleView()
        }
    }

    private fun handleList(resultList: ResultList) {
        console.log("[ListObserver.handleList] obj == ResultList")
        val result = resultList.result!!
        val members = result.value
        for (l: Link in members) {
            console.log("[ListObserver.handleList] link:\n $l")
            l.invoke(this)
        }
    }

    private fun handleView() {
        val title: String = this::class.simpleName.toString()
        //TODO on runFixtureScript this is passed multiple times  
        if (isRendered) {
            console.log("[ListObserver.handleView] already opened: $title")
        } else {
            console.log("[ListObserver.handleView] about to open: $title")
            val model = list.list
            console.log("[ListObserver.handleView] number of objects: ${model.size}")
            val panel = FixtureResultTable(model)
            val icon = IconManager.find(title)
            RoView.addTab(title, panel, icon = icon)
            // UiManager.addView(list) 
            isRendered = true
        }
    }

    private fun handleProperty(p: Property) {
        //TODO differentiate between Property and PropertyDescription
        val ext = p.extensions!!
        if (ext.friendlyName.isNotEmpty()) {
            console.log("[ListObserver.handleProperty] -> description")
        } else {
            console.log("[ListObserver.handleProperty]")
            val descLink = p.descriptionLink()!!
            descLink.invoke(this)
            list.addProperty(p)
        }
    }

    private fun handleObject(obj: TObject) {
        console.log("[ListObserver.handleObject] adding TObject:\n ${obj}")
        list.list.add(ObjectAdapter(obj))
        if (!list.hasLayout()) {
            val link = obj.getLayoutLink()!!
            link.invoke(this)
        }
    }

}

fun Property.descriptionLink(): Link? {
    val answer = links.find {
        it.rel == RelType.DESCRIBEDBY.type
    }
    return answer
}
