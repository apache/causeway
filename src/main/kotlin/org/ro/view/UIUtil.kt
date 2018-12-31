package org.ro.view

import kotlinx.serialization.json.JsonObject

class UIUtil {
    fun decorate(result: UIComponent, s: String, debugInfo: JsonObject?) {
    }

    fun buildFormItem(toString: Any): FormItem? {
        return FormItem()
    }
}