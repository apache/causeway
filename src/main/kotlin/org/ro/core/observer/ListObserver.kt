package org.ro.core.event

import kotlinx.serialization.Serializable
import org.ro.core.UiManager
import org.ro.core.Utils
import org.ro.core.model.Exposer
import org.ro.core.model.ObjectList
import org.ro.core.observer.BaseObserver
import org.ro.layout.Layout
import org.ro.to.Link
import org.ro.to.Property
import org.ro.to.ResultList
import org.ro.to.TObject
import org.ro.view.table.fr.FixtureResultTable

/** sequence of operations:
 * (0) list
 * (1) FR_OBJECT                TObjectHandler -> invoke()
 * (2) FR_OBJECT_LAYOUT         layoutHandler -> invoke(layout.getProperties()[].getLink()) link can be null?
 * (3) FR_OBJECT_PROPERTY       PropertyHandler -> invoke()
 * (4) FR_PROPERTY_DESCRIPTION  PropertyDescriptionHandler      //FIXME PropertyDescription to be used for table layout
 */
@Serializable
class ListObserver : BaseObserver() {
    var list = ObjectList()
    private var isRendered = false;

    // Handlers should set object into le after successful parsing
    override fun update(logEntry: LogEntry) {
        val obj = logEntry.getObj()

        when (obj) {
            is ResultList -> handleList(obj)
            is TObject -> handleObject(obj)
            is Layout -> list.layout = obj
            is Property -> handleProperty(obj)
            else -> log(logEntry)
        }

        if (list.hasLayout() && !isRendered) {
            handleView()
        }
    }

    private fun handleList(resultList: ResultList) {
        val result = resultList.result!!
        val members = result.value
        for (l: Link in members) {
            invoke(l)
        }
    }

    private fun handleView() {
        val title: String = this::class.simpleName.toString()
        val model = mutableListOf<Exposer>()
        for (i in list.list) {
            model.add(i.dynamise())
        }
        val panel = FixtureResultTable(model)
        UiManager.add(title, panel)
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
                invoke(descLink)
                list.addProperty(p)
            }
        }
    }

    private fun handleObject(obj: TObject) {
        list.list.add(Exposer(obj).dynamise())
        if (!list.hasLayout()) {
            val link = obj.getLayoutLink()
            if (link != null) {
                invoke(link)
            }
        }
    }

}
