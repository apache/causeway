package org.ro.layout

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import org.ro.view.VBox

/**
 * Parse layout specification.
 * In case of non-menu layout, build a UIComponent.
 */
@Serializable
data class Layout(val cssClass: String? = null,
                  val row: List<RowLayout> = emptyList()) {

    @Optional
    var propertyLabels = mutableMapOf<String, String>()
    @Optional
    var properties: List<PropertyLayout>? = null

    init {
        console.log("[number of rows: ${row.size}]")
        val row1 = row[1]
        var cols = row1.cols[0]
        val tabGroup = cols.col?.tabGroup
        if (tabGroup != null) {
            val tabGroup0 = tabGroup[0]
            val tab0 = tabGroup0.tab[0]
            val row0 = tab0.row[0]
            cols = row0.cols[0]
        }
        val col = cols.col!!
        val fieldSet0 = col.fieldSet[0]
        properties = fieldSet0.property
    }

    fun addPropertyLabel(id: String, friendlyName: String) {
        propertyLabels.put(id, friendlyName)
    }

    fun getPropertyLabel(id: String): String? {
        return propertyLabels.get(id)
    }

    fun arePropertyLabelsToBeSet(): Boolean {
        val labelSize: Int = propertyLabels.size
        var propsSize = 0
        if (properties!!.isNotEmpty()) {
            propsSize = properties!!.size
        }
        return (labelSize < propsSize)
    }

    fun build(): VBox {
        val result = VBox("Layout" )
        var b: VBox
        for (rl in row) {
            // row[0] (head) contains the object title and actions (for wicket viewer)
            // this is to be handled differently (tab)
            b = rl.build()      
            result.addChild(b)
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
