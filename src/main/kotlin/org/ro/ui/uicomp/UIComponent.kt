package org.ro.ui.uicomp

open class UIComponent(open val label: String = "not set") {
    var children = mutableListOf<UIComponent>()

    fun addChild(uic: UIComponent) {
        children.add(uic)
        //       console.log("[UIComponent: ${uic.label} added to: $label (${children.size})]")
    }

}
