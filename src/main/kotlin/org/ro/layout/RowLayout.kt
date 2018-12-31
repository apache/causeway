package org.ro.layout

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import org.ro.view.Box
import org.ro.view.UIUtil
import org.ro.view.VBox

class RowLayout(jsonObj: JsonObject? = null) : AbstractLayout() {
    private val maxSpan = 12

    private lateinit var cols: JsonArray
    private var metadataError: String? = null
    private var id: String? = null

    private var columnList = mutableListOf<ColLayout>()

    init {
        if (jsonObj != null) {
            cols = jsonObj["cols"].jsonArray
            for (json in cols) {
                val l = ColLayout(json as JsonObject)
                columnList.add(l)
            }
            metadataError = jsonObj["metadataError"].toString()
            id = jsonObj["id"].toString()
        }
    }

    fun ensureMaxSpan(): Boolean {
        var sum = 0
        for (c in columnList) {
            sum += c.span
        }
        return maxSpan == sum
    }

    fun build(): VBox {
        val result = VBox()
        result.label = "tab: $id"
        UIUtil().decorate(result, "RowLayout", debugInfo)
        var b: Box
        for (c in columnList) {
            b = c.build()
            result.addChild(b)
        }
        return result
    }

}