package org.apache.isis.client.kroviz.ui

import org.apache.isis.client.kroviz.to.ValueType
import org.apache.isis.client.kroviz.ui.kv.RoDialog
import org.apache.isis.client.kroviz.utils.DomHelper
import org.apache.isis.client.kroviz.utils.ScalableVectorGraphic
import org.apache.isis.client.kroviz.utils.UmlUtils
import org.w3c.dom.parsing.DOMParser
import pl.treksoft.kvision.core.onEvent
import kotlin.js.Date

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

    private val uuid: String = Date().toTimeString() //IMPROVE
    private var dialog: RoDialog

    fun open() {
        dialog.open()
        UmlUtils.generateDiagram(pumlCode, uuid)
    }

    init {
        val formItems = mutableListOf<FormItem>()
        val img = FormItem("svg", ValueType.IMAGE.type, callBackId = uuid)
        formItems.add(img)

        dialog = RoDialog(
                widthPerc = 80,
                caption = label,
                items = formItems,
                command = this,
                heightPerc = 80)
        console.log("[ImageDialog.init] $dialog")
 //       if (dialog.hasScalableContent()) {
            dialog.onEvent {
                keypress = { e ->
                    console.log("[ImageDialog.open] keydown")
                    console.log(e)
                    if (e.key === "+" && e.ctrlKey) {
                        scale(true)
                        console.log("[<CTRL>-<Alt>-<+>]")
                        e.stopPropagation()
                        e.preventDefault()
                    }
                    if (e.key === "-" && e.ctrlKey) {
                        scale(false)
                        console.log("[<CTRL>-<Alt>-<->]")
                        e.stopPropagation()
                        e.preventDefault()
                    }
                }
            }
 //       }
    }


    private fun scale(upOrDown: Boolean) {
        val mimeType = "image/svg+xml"
        val oldElement = DomHelper.getById(uuid)
        val oldStr = oldElement.toString()
        console.log(oldStr)
        val p = DOMParser()
        var svg = p.parseFromString(oldStr, mimeType)
        val image = ScalableVectorGraphic(svg)
        if (upOrDown) {
            image.scaleUp()
        } else {
            image.scaleDown()
        }
        val newStr = image.serialized()
        svg = p.parseFromString(newStr, mimeType)
        DomHelper.replaceWith(uuid, svg.documentElement!!)
    }

}

