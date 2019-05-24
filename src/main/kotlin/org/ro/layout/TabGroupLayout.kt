package org.ro.layout

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable
import org.ro.view.uicomp.TabNavigator
import org.ro.view.uicomp.UIComponent

@Serializable
data class TabGroupLayout(val cssClass: String? = "",        
                          val metadataError: String? = "",   
                          @Optional val tab: List<TabLayout> = emptyList(),
                          val unreferencedCollections: Boolean? = false) {

    fun build(): UIComponent {
        val result = TabNavigator("TabGroupLayout")
        result.percentWidth = 100
        result.percentHeight = 100
        result.tabFocusEnabled = true

        var b: UIComponent
        for (tl in tab) {
            b = tl.build()
            result.addChild(b)
        }
        return result
    }

}