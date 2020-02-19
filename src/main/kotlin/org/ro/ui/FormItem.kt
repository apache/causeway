package org.ro.ui

import org.ro.to.Member
import org.ro.to.ValueType
import pl.treksoft.kvision.window.Window

class FormItem(
        val label: String,
        val type: String,
        var content: Any? = null,
        val size: Int? = null,
        val description: String? = "not set",
        val member: Member? = null,
        var dspl: Displayable? = null,
        val callBackId: String? = null) {

    private var originalContent: Any?
    var readOnly = false

    init {
        originalContent = content
        if (member != null) {
            readOnly = member.isReadOnly()
        }
    }

    fun changed(value: String?) {
        dspl?.setDirty(true)
        if (member != null) {
            member.value?.content = value
        }
        setOpacity(value)
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

    fun setOpacity(value: String?) {
        if (type == ValueType.SLIDER.type) {
            val opacity = value?.toDouble()!!
            //(dspl as Window).opacity = opacity
            (dspl as Window).getElementJQuery()?.css(
                    "background-color",
                    "rgba(255, 255, 255, $opacity)"
            )
        }
    }

}
