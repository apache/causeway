package org.ro.layout

import kotlinx.serialization.Serializable
import org.ro.view.TabNavigator
import org.ro.view.UIComponent
import org.ro.view.UIUtil
import pl.treksoft.kvision.utils.Object

@Serializable
data class TabGroupLayout(val cssClass: String? = null,        //AbstractLo
                          val metadataError: String? = null,   //MemberLo
                          val tab: List<TabLayout>? = null,
                          val unreferencedCollections: Object? = null  //TODO not included in SimpleApp
) {

    fun build(): UIComponent? {
        val result = TabNavigator()
        result.percentWidth = 100
        result.percentHeight = 100
        result.tabFocusEnabled = true

        UIUtil().decorate(result, "TabGroupLayout", "debug")
        var b: UIComponent?
        //FIXME tab has (General, Metadata, Other) but rowlist is not initialized
        for (tl in tab!!) {
            b = tl.build()
            result.addChild(b)
        }
        return result
    }

}