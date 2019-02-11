package org.ro.layout

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import org.ro.view.Box
import org.ro.view.UIUtil
import org.ro.view.VBox

/**
 * Parse layout specification.
 * In case of non-menu layout, build a UIComponent.
 */
@Serializable
data class Layout(val cssClass: String? = null,
             val row: List<RowLayout> = emptyList()) {

    @Optional
    var propertyLabels: List<String>? = null
    @Optional
    var properties: List<PropertyLayout>? = null
    @Optional
    val TAB_GROUP: String = "tabGroup"

    init {
        if (row != null) {
            val rowLo = row[1]
            var colLo = rowLo.cols!![0]
/* FIXME
            if (colLo.tabGroup != null) {
                // special case for json1
                val tgLo = colLo.tabGroup!![0]
                val rLo = tgLo.row[0]
                colLo = rLo.cols!![0]
            }      
            properties = colLo.fieldSet!![0].property   */
        }
    }

    fun addPropertyLabel(id: String, friendlyName: String) {
//FIXME        propertyLabels[id) = friendlyName
    }

    fun getPropertyLabel(id: String): String {
//FIXME        return propertyLabels[id)
        return ""
    }

    fun arePropertyLabelsToBeSet(): Boolean {
        val labelSize: Int = propertyLabels!!.size
        var propsSize = 0
//FIXME        if (properties.isNotEmpty()) {
        //     propsSize = properties.size
        // }
        return (labelSize < propsSize)
    }

    fun build(): VBox {
        val result = VBox()
        UIUtil().decorate(result, "Layout", "debug")
        var b: Box
        //TODO iterate over rows, recurse into columns, etc.
        var rowCount: Int = 0
        for (rl in row!!) {
            rowCount += 1
            // the first row contains the object titel and actions (for wicket viewer)
            // this is handled here differently (tab)
            if (rowCount > 1) {
                b = rl.build()
                result.addChild(b)
            }
        }
        return result
    }

    // recurse into attributes/fields/properties and 
    fun findPropertyBy(
            obj: JsonObject, name: String): JsonObject? {
        //FIXME
        /*
        if (obj.hasOwnProperty(name)) {
            return obj[name]
        } else {
            for (o in obj) {
                if (isCollection(o) || isNamed(o)) {
                    return findPropertyBy(o, name)
                }
            }
        }  */
        return null

    }

    /*
    private fun isCollection(o: JsonObject): Boolean {
        if ((o == null) || (o is String)) {
            return false
        } else {
            return ((o is Object) || (o is Array))
        }
    }

    private fun isNamed(o: JsonObject): Boolean {
        if ((o == null) || (o is String)) {
            return false
        } else {
            var TAB_GROUP: String = "tabGroup"
            return ((o.hasOwnProperty("col")) || (o.hasOwnProperty(TAB_GROUP)))
        }
    }   */
}
