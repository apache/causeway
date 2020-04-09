package org.apache.isis.client.kroviz.ui

import org.apache.isis.client.kroviz.to.ValueType
import org.apache.isis.client.kroviz.ui.kv.RoDialog
import org.apache.isis.client.kroviz.utils.UmlUtils
import kotlin.js.Date

class ImageDialog(var label: String = defaultLabel, var pumlCode: String = defaultPumlCode) : Command {

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

        val dialog = RoDialog(
                caption = label,
                items = formItems,
                command = this,
                widthPerc = 80,
                heightPerc = 80)
        dialog.open()
        slider.setDisplay(dialog)

        UmlUtils.generateDiagram(pumlCode, uuid)
    }

}

