package org.ro.ui.kv

import org.ro.core.model.DisplayObject
import org.ro.ui.FormItem
import pl.treksoft.kvision.form.FormPanel
import pl.treksoft.kvision.panel.VPanel

class RoDisplay(displayObject: DisplayObject) : VPanel() {

    var panel: FormPanel<String>?

    init {
        val model = displayObject.data!!
        val items: MutableList<FormItem> = mutableListOf<FormItem>()
        val tObject = model.delegate
        val members = tObject.members
        members.forEach { it ->
            if (it.value.memberType == "property") {
                val label = it.key
                val type = "Text" //it.value.memberType
                var content = it.key
                val value = it.value.value
                if (value != null) {
                    content = value.content.toString()
                }
                val item = FormItem(label, type, content)
                items.add(item)
            }
        }

        panel = FormPanelFactory(items).panel
//        panel?.let { add(it) }

        val ui = displayObject.layout!!.build(members)
        console.log("[RoDisplay.init]")
        console.log(displayObject)
        add(ui)

    }

}
