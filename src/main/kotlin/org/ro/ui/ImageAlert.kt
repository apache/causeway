package org.ro.ui

import org.ro.to.ValueType
import org.ro.ui.kv.RoDialog
import org.ro.utils.UmlUtils
import kotlin.js.Date

class ImageAlert(var label: String = defaultLabel, var pumlCode: String = defaultPumlCode) : Command {

    companion object {
        val defaultLabel = "UML Diagram Sample"
        val defaultPumlCode = "\"" +
                "participant BOB [[https://en.wiktionary.org/wiki/best_of_breed]]\\n" +
                "participant PITA [[https://en.wiktionary.org/wiki/PITA]]\\n" +
                "BOB -> PITA: sometimes is a" +
                "\""
    }

    private val uuid: String = Date().toTimeString() //IMPROVE

    fun open() {
        val formItems = mutableListOf<FormItem>()
        val slider = FormItem("Opacity", ValueType.SLIDER.type, content = 1.0)
        formItems.add(slider)

        val img = FormItem("svg", ValueType.IMAGE.type, callBackId = uuid)
        formItems.add(img)

        val dialog = RoDialog(caption = label, items = formItems, command = this)
        dialog.open()
        slider.setDisplay(dialog)

        UmlUtils.generateDiagram(pumlCode, uuid)
    }

}

