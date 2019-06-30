package org.ro.view.table

import org.ro.core.Utils
import org.ro.core.event.LogEntry
import kotlin.reflect.KProperty1

/**
 * Column specification
 */
class ColDef(val property: KProperty1<LogEntry, Any?>,
             var width: Int = 10,
             var name: String = "",
             val tip: String? = null) {          //DateFormatter
    init {
        if (name.length == 0) {
            name = Utils.deCamel(property.name);
        }
        if (width < 10) {
            width = name.length
        }
    }

    fun hasTip(): Boolean {
        return (tip != null);
    }

}
