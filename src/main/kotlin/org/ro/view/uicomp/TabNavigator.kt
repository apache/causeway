package org.ro.view.uicomp

class TabNavigator(override val label: String) : UIComponent() {
    var percentWidth: Int = 100
        set
    var percentHeight: Int = 100
        set
    var tabFocusEnabled: Boolean = true
        set

}