package org.ro.layout

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import org.ro.view.TabNavigator
import org.ro.view.UIComponent
import org.ro.view.UIUtil
import org.ro.view.VBox

class TabLayout(jsonObj: JsonObject? = null) : AbstractLayout() {
    private var name: String? = null
    private lateinit var row: JsonArray // which actually is a list of rows
    private var unreferencedCollections: JsonArray? = null
    private var tab: JsonArray? = null  // is a list of tabs
    private var metadataError: JsonObject? = null

    private var rowList = mutableListOf<RowLayout>()

    init {
        if (jsonObj != null) {
            row = jsonObj["row"].jsonArray
            for (json in row) {
                val rl = RowLayout(json as JsonObject)
                rowList.add(rl)
            }
            name = jsonObj["name"].toString()
            unreferencedCollections = jsonObj["unreferencedCollections"].jsonArray
            tab = jsonObj["tab"].jsonArray
            metadataError = jsonObj["metadataError"].jsonObject
        }
    }

    fun build(): UIComponent? {
        val result = TabNavigator()
        result.percentWidth = 100
        result.percentHeight = 100
        result.tabFocusEnabled = true

        UIUtil().decorate(result, "TabLayout", debugInfo)
        var b: VBox
        //FIXME tab has (General, Metadata, Other) but rowlist is not initialized
        for (rl in rowList) {
            b = rl.build()
            result.addChild(b)
        }
        return result
    }

}