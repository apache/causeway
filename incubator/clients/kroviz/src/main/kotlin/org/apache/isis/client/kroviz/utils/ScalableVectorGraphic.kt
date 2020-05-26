package org.apache.isis.client.kroviz.utils

import org.w3c.dom.Document
import org.w3c.dom.parsing.DOMParser

enum class Direction(val id: String) {
    UP("UP"),
    DOWN("DOWN")
}

class ScalableVectorGraphic(val data: String) {

    private val parser = DOMParser()
    private val mimeType = "image/svg+xml"
    private val tag = "viewBox"
    var document: Document
    var viewBox: ViewBox

    init {
        document = parser.parseFromString(data, mimeType)

        val root = document.rootElement!!
        val raw = root.getAttribute(tag) as String
        val arr = raw.split(" ")
        viewBox = ViewBox(arr[0].toInt(), arr[1].toInt(), arr[2].toInt(), arr[3].toInt())
    }

    private fun setCorner(width: Int, height: Int) {
        viewBox.width = width
        viewBox.height = height
        document.rootElement?.setAttribute(tag, viewBox.asArgs())
    }

    fun scaleUp(factor: Double = 0.1) {
        var f = factor
        if (factor < 1) f = 1 + factor
        val newWidth = (viewBox.width * f).toInt()
        val newHeight = (viewBox.height * f).toInt()
        setCorner(newWidth, newHeight)
    }

    fun scaleDown(factor: Double = 0.1) {
        scaleUp(factor * -1)
    }

    class ViewBox(val x: Int, val y: Int, var width: Int, var height: Int) {
        fun asArgs(): String {
            return "$x $y $width $height"
        }
    }

}
