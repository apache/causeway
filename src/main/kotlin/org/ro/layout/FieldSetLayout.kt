package org.ro.layout

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import org.ro.view.FormItem
import org.ro.view.HBox
import org.ro.view.UIUtil

class FieldSetLayout(jsonObj: JsonObject? = null) : AbstractLayout() {
     private var name: String? = null
     private var action: JsonObject? = null  // this is a list of actions
     private var property: JsonArray // this is a list of properties
     private var metadataError: String? = null
     private var id: String? = null
     private var unreferencedActions: JsonArray? = null
     private var unreferencedCollections: JsonArray? = null

    init {
        name = jsonObj!!["name"].toString()
        action = jsonObj["action"].jsonObject
        property = jsonObj["property"].jsonArray
        metadataError = jsonObj["metadataError"].toString()
        id = jsonObj["id"].toString()
        unreferencedActions = jsonObj["unreferencedActions"].jsonArray
        unreferencedCollections = jsonObj["unreferencedCollections"].jsonArray
    }

    fun build(): HBox {
        val result = HBox()
        UIUtil().decorate(result, "FieldSetLayout", debugInfo)
        var fi: FormItem?
        val form: org.ro.view.Form = org.ro.view.Form()
        for (p in property) {
            fi = UIUtil().buildFormItem(p.toString())
            form.addElement(fi)
        }
        result.addChild(form)
        return result
    }

}