package org.apache.isis.client.kroviz.ui

import org.apache.isis.client.kroviz.to.Member

class FormItem(
        val label: String,
        val type: String,
        var content: Any? = null,
        val size: Int? = null,
        val description: String? = "not set",
        val member: Member? = null,
        var dspl: Displayable? = null,
        val callBackId: String? = null) {

    private var originalContent: Any? = content
    var readOnly = false

    init {
        if (member != null) {
            readOnly = member.isReadOnly()
        }
    }

    fun changed(value: String?) {
        dspl?.setDirty(true)
        if (member != null) {
            member.value?.content = value
        }
    }

    fun reset() {
        dspl?.setDirty(false)
        if (member != null) {
            content = originalContent
        }
    }

    fun setDisplay(displayable: Displayable) {
        dspl = displayable
    }

}
