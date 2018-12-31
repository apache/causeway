package org.ro.layout

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import org.ro.view.Box
import org.ro.view.UIUtil
import org.ro.view.VBox

/**
 * Parse layout specification.
 * In case of non-menu layout, build a UIComponent.
 */
class Layout(jsonObj: JsonObject? = null) : AbstractLayout(jsonObj) {
    private var rowList = mutableListOf<RowLayout>()
    var properties = mutableListOf<PropertyLayout>()
    private var propertyLabels: JsonObject? = null

    init {
        if (jsonObj != null) {
            val row = jsonObj["row"].jsonArray  // which actually is a list of rows
            val props = extractProperties(row)
//            initProperties(props)
            for (json in row) {
                val l = RowLayout(json as JsonObject)
                this.rowList.add(l)
            }
        }
    }

    private fun extractProperties(row: JsonArray) {
        //TODO refactor train.wreck.s  
        // var s:Object = findPropertyBy(row, "fieldSet")
        //FIXME
        /*
        var col: Object = row[1].cols[0].col
        val TAB_GROUP: String = "tabGroup"
        if (col.ta) {
            // special case for json1
            col = col.tabGroup[0].tab[0].row[0].cols[0].col
        }
        val pArr = col.fieldSet[0].property
        return pArr
        */
        return 
    }

    private fun initProperties(props: JsonArray) {
        for (json in props) {
            val pl = PropertyLayout(json as JsonObject)
            this.properties.add(pl)
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
        if (properties.isNotEmpty()) {
            propsSize = properties.size
        }
        return (labelSize < propsSize)
    }

    fun build(): VBox {
        val result = VBox()
        UIUtil().decorate(result, "Layout", debugInfo)
        var b: Box
        //TODO iterate over rows, recurse into columns, etc.
        var rowCount: Int = 0
        for (rl in rowList) {
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
    private fun findPropertyBy(
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