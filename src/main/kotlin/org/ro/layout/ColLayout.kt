package org.ro.layout

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import org.ro.view.HBox
import org.ro.view.UIComponent
import org.ro.view.UIUtil

class ColLayout(jsonObj: JsonObject? = null) : AbstractLayout() {
    private lateinit var col: JsonObject
    private var domainObject: JsonObject? = null
    private var metadataError: String? = null
    var size: Int? = null
    var id: String? = null
    var span: Int = 0
    private var unreferencedActions: JsonObject? = null
    private var unreferencedCollections: JsonObject? = null
    private lateinit var tabGroup: JsonArray
    private lateinit var fieldSet: JsonArray

    private var fieldList = mutableListOf<FieldSetLayout>()
    private var tabList = mutableListOf<TabLayout>()

    init {
        if (jsonObj != null) {
            col = jsonObj["col"].jsonObject
            domainObject = jsonObj["domainObject"].jsonObject
            metadataError = jsonObj["metadataError"].toString()
            size = jsonObj["size"].int
            id = jsonObj["id"].toString()
            col = jsonObj["col"].jsonObject
            col = jsonObj["col"].jsonObject
            span = jsonObj["span"].int
            unreferencedActions = jsonObj["unreferencedActions"].jsonObject
            unreferencedCollections = jsonObj["unreferencedCollections"].jsonObject
            fieldSet = jsonObj["fieldSet"].jsonArray
            for (json in fieldSet) {
                val fsl = FieldSetLayout(json as JsonObject)
                fieldList.add(fsl)
            }
            col = jsonObj["col"].jsonObject
            tabGroup = col["tabGroup"].jsonArray 
            for (json2 in tabGroup) {
                val t = TabLayout(json2 as JsonObject)
                tabList.add(t)
            }
        }
    }

    fun build(): HBox {
        val result = HBox()
        UIUtil().decorate(result, "ColLayout", debugInfo)
        var b: UIComponent?
        for (tl in tabList) {
            b = tl.build()
            result.addChild(b)
        }
        for (fsl in fieldList) {
            b = fsl.build()
            result.addChild(b)
        }
        // actions will not be rendered as buttons
        return result
    }

}