package org.ro.layout

import kotlinx.serialization.Serializable
import org.ro.to.bs3.TabGroup
import org.ro.ui.uicomp.TabNavigator
import org.ro.ui.uicomp.UIComponent

@Serializable
data class TabGroupLayout(val cssClass: String? = "",
                          val metadataError: String? = "",
                          val tab: MutableList<TabLayout> = mutableListOf<TabLayout>(),
                          val unreferencedCollections: Boolean? = false
) {
    constructor(tabGroup: TabGroup) : this() {
        tabGroup.tabs.forEach {
            tab.add(TabLayout(it))
        }
    }

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
