package org.ro.layout

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable
import org.ro.view.TabNavigator
import org.ro.view.UIComponent
import org.ro.view.UIUtil
import org.ro.view.VBox

@Serializable
data class TabLayout(val cssClass: String? = null,     
                     val name: String? = null,
                     @Optional val row: List<RowLayout> = emptyList()) {

    fun build(): UIComponent? {
        val result = TabNavigator()
        result.percentWidth = 100
        result.percentHeight = 100
        result.tabFocusEnabled = true

        UIUtil().decorate(result, "TabLayout", "debug")
        var b: VBox
        //FIXME tab has (General, Metadata, Other) but rowlist is not initialized
/*        for (rl in row) {
            b = rl.build()
            result.addChild(b)
        }*/
        return result
    }

}