package org.apache.isis.client.kroviz.ui

import org.apache.isis.client.kroviz.to.ValueType
import org.apache.isis.client.kroviz.ui.kv.RoDialog
import org.apache.isis.client.kroviz.utils.Direction
import org.apache.isis.client.kroviz.utils.DomHelper
import org.apache.isis.client.kroviz.utils.ScalableVectorGraphic
import org.apache.isis.client.kroviz.utils.UmlUtils

@ExperimentalUnsignedTypes
class ImageDialog(
        var label: String = defaultLabel,
        var pumlCode: String = defaultPumlCode) : Command {

    companion object {
        val defaultLabel = "UML Diagram Sample"
        val defaultPumlCode = "\"" +
                "participant BOB [[https://en.wiktionary.org/wiki/best_of_breed]]\\n" +
                "participant PITA [[https://en.wiktionary.org/wiki/PITA]]\\n" +
                "BOB -> PITA: sometimes is a" +
                "\""
    }

    val uuid: String = DomHelper.uuid()
    private var dialog: RoDialog
    val formItems = mutableListOf<FormItem>()

    fun open() {
        dialog.open()
        UmlUtils.generateDiagram(pumlCode, uuid)
    }

    init {
        val img = FormItem("svg", ValueType.IMAGE.type, callBackId = uuid)
        formItems.add(img)

        dialog = RoDialog(
                widthPerc = 80,
                caption = label,
                items = formItems,
                command = this,
                heightPerc = 80)
    }

    fun scale(direction: Direction) {
        val oldElement = DomHelper.getById(uuid)
        val oldStr = oldElement!!.innerHTML
        console.log(oldStr)
        val newImage = ScalableVectorGraphic(oldStr)
        when (direction) {
            Direction.UP -> newImage.scaleUp()
            Direction.DOWN -> newImage.scaleDown()
        }
        DomHelper.replaceWith(uuid, newImage)
    }

}

