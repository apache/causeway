package org.apache.isis.client.kroviz.ui

import org.apache.isis.client.kroviz.to.ValueType
import org.apache.isis.client.kroviz.ui.kv.RoDialog
import org.apache.isis.client.kroviz.utils.Direction
import org.apache.isis.client.kroviz.utils.DomHelper
import org.apache.isis.client.kroviz.utils.ScalableVectorGraphic
import org.apache.isis.client.kroviz.utils.UmlUtils

class ImageDialog(
        var label: String,
        private var pumlCode: String) : Command() {

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
                caption = "Diagram",
                items = formItems,
                command = this)
    }

    fun scale(direction: Direction) {
        val oldElement = DomHelper.getById(uuid)!!
        val oldStr = oldElement.innerHTML
        val newImage = ScalableVectorGraphic(oldStr)
        when (direction) {
            Direction.UP -> newImage.scaleUp()
            Direction.DOWN -> newImage.scaleDown()
        }
        DomHelper.replaceWith(uuid, newImage)
    }

}
