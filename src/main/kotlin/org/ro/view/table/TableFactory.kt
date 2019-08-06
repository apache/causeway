package org.ro.org.ro.view.table

import org.ro.core.model.Revealator
import org.ro.to.Extensions
import org.ro.to.TObject
import pl.treksoft.kvision.utils.ObservableList
import pl.treksoft.kvision.utils.observableListOf

/**
 * Create ColumnDefinitions for Tabulator tables based on result of REST service invocations
 */
class TableFactory {
    fun testData(): ObservableList<Revealator> {
        val answer = observableListOf<Revealator>()
        val t1 = TObject(extensions = Extensions())
        val o1 = Revealator(t1)
        val d1 = o1.asDynamic()
        d1["var1"] = "string1"
        d1["var2"] = 1
        answer.add(d1)

        val t2 = TObject(extensions = Extensions())
        val o2 = Revealator(t2)
        val d2 = o2.asDynamic()
        d2["var1"] = "string2"
        d2["var2"] = 2
        answer.add(d2)

        return answer
    }
}
