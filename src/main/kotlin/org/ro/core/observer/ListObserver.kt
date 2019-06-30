package org.ro.core.event

import kotlinx.serialization.Serializable
import org.ro.core.UiManager
import org.ro.core.Utils
import org.ro.core.model.ObjectAdapter
import org.ro.core.model.ObjectList
import org.ro.core.observer.BaseObserver
import org.ro.layout.Layout
import org.ro.to.*
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

        if (list.hasLayout() && !isRendered) {
            handleView()
        }
    }

    private fun handleList(resultList: ResultList) {
        val result = resultList.result!!
        val members = result.value
        for (l: Link in members) {
            l.invoke(this)
        }
    }

    private fun handleView() {
        val title: String = this::class.simpleName.toString()
        console.log("[ListObserver.handleView] about to open: $title")
        val model = list.list
        val panel = FixtureResultTable(model)
        UiManager.addView(title, panel)
        isRendered = true
    }

    private fun handleProperty(p: Property) {
        //TODO differentiate between Property and PropertyDescription
        if (p.extensions == null) {
            Utils.debug(p)
        } else {
            val ext = p.extensions
            if (ext.friendlyName.isNotEmpty()) {
                console.log("[ListObserver.handleProperty] -> description")
            } else {
                console.log("[ListObserver.handleProperty]")
                val descLink = p.descriptionLink()!!
                descLink.invoke(this)
                list.addProperty(p)
            }
        }
    }

    private fun handleObject(obj: TObject) {
        list.list.add(ObjectAdapter(obj))
        console.log("[ListObserver.handleObject] number of objects: ${list.list.size}")
        if (!list.hasLayout()) {
            val link = obj.getLayoutLink()
            if (link != null) {
                link.invoke(this)
            }
        }
    }

}

fun Property.descriptionLink(): Link? {
    val answer = links.find {
        it.rel == RelType.DESCRIBEDBY.type
    }
    return answer
}
