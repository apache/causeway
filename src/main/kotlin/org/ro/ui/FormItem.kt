package org.ro.ui

import org.ro.ui.kv.RoDisplay

class FormItem(
        val label: String,
        val type: String,
        val content: Any? = null,
        val size: Int = 1,
        val description: String? = "not set",
        val tab: RoDisplay? = null) {
    fun changed() {
        if (tab != null) {
            tab.setDirty(true)
        }
    }
}
