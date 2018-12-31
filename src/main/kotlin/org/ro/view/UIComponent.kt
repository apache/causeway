package org.ro.view

open class UIComponent() {
    var label: String = ""

    fun addChild(form: UIComponent?) {

    }

    fun getChildren(): MutableList<UIComponent> {
        return mutableListOf()
    }
}