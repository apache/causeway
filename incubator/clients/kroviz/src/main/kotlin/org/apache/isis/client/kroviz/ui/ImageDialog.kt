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
        private var pumlCode: String = defaultPumlCode) : Command {

    companion object {
        const val defaultLabel = "UML Diagram Sample"
        const val defaultPumlCode = "\"" +
                "participant BOB [[https://en.wiktionary.org/wiki/best_of_breed]]\\n" +
                "participant PITA [[https://en.wiktionary.org/wiki/PITA]]\\n" +
                "BOB -> PITA: sometimes is a" +
                "\""
    }

    private val uuid: String = DomHelper.uuid()
    private var dialog: RoDialog
    private val formItems = mutableListOf<FormItem>()

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
        val newImage = ScalableVectorGraphic(oldStr)
        when (direction) {
            Direction.UP -> newImage.scaleUp()
            Direction.DOWN -> newImage.scaleDown()
        }
        DomHelper.replaceWith(uuid, newImage)
    }

}

