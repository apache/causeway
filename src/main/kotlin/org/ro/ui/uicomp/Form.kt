package org.ro.ui.uicomp

class Form(override val label: String) : UIComponent() {
    fun addElement(fi: FormItem?) {
        console.log("[Form.addElement] $fi")
    }
}
