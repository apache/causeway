package org.ro.org.ro.view.table

import org.ro.core.model.ObjectAdapter
import org.ro.to.Extensions
import org.ro.to.TObject
import pl.treksoft.kvision.utils.ObservableList
import pl.treksoft.kvision.utils.observableListOf

/**
 * Create ColumnDefinitions for Tabulator tables based on result of REST service invocations
 */
class TableFactory {
    fun testData(): ObservableList<ObjectAdapter> {
        val answer = observableListOf<ObjectAdapter>()
        val d1 = TObject(extensions = Extensions())
        val o1 = ObjectAdapter(d1)
        val dynObj = o1.asDynamic()
        dynObj["var1"] = "value1"
        answer.add(dynObj)
        return answer
    }
}
