package org.ro.ui

import org.ro.to.Member
import org.ro.ui.kv.RoDisplay

class FormItem(
        val label: String,
        val type: String,
        var content: Any? = null,
        val size: Int = 1,
        val description: String? = "not set",
        val member: Member? = null,
        val tab: RoDisplay? = null) {

    private var originalContent: Any?
    var readOnly = false

    init {
        originalContent = content
        if (member != null) {
            readOnly = member.isReadOnly()
        }
    }

    fun changed(value: String?) {
        tab?.setDirty(true)
        if (member != null) {
            member.value?.content = value
        }
    }

    fun reset() {
        tab?.setDirty(false)
        if (member != null) {
            content = originalContent
        }
    }

}
