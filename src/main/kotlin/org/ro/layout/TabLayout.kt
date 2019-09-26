package org.ro.layout

import kotlinx.serialization.Serializable
import org.ro.ui.uicomp.TabNavigator
import org.ro.ui.uicomp.UIComponent
import org.ro.ui.uicomp.VBox

@Serializable
data class TabLayout(val cssClass: String? = null,
                     val name: String? = null,
                     val row: List<RowLayout> = emptyList()) {

    fun build(): UIComponent {
        val result = TabNavigator("TabLayout")
        result.percentWidth = 100
        result.percentHeight = 100
        result.tabFocusEnabled = true

        var b: VBox
        for (rl in row) {
            b = rl.build()
            result.addChild(b)
        }
        return result
    }

}
