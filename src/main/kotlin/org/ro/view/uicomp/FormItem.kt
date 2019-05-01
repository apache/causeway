package org.ro.view.uicomp

import org.ro.view.uicomp.UIComponent

class FormItem(override val label: String, val type: String, val content: Any? = null, val size: Int = 1) : UIComponent() {
}