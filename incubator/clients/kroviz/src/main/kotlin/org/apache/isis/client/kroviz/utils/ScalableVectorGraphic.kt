package org.apache.isis.client.kroviz.utils

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.parsing.DOMParser
import org.w3c.dom.parsing.XMLSerializer
import org.w3c.dom.svg.SVGSVGElement

enum class Direction(val id: String) {
    UP("UP"),
    DOWN("DOWN")
}

class ScalableVectorGraphic(val data: String) {

    val parser = DOMParser()
    val mimeType = "image/svg+xml"
    var document: Document

    init {
        document = parser.parseFromString(data, mimeType)
    }

    fun asDocumentElement(): Element? {
        val xs = XMLSerializer()
        val str = xs.serializeToString(document)
        val svg = parser.parseFromString(str, mimeType)
        return svg.documentElement
    }

    private fun root(): SVGSVGElement {
        return document.rootElement!!
    }

    fun setHeight(height: Int) {
        root().setAttribute("height", height.toString() + "px")
    }

    fun getHeight(): Int {
        val raw = root().getAttribute("height") as String
        val value = raw.replace("px", "")
        return value.toInt()
    }

    fun setWidth(width: Int) {
        root().setAttribute("width", width.toString() + "px")
    }

    fun getWidth(): Int {
        val raw = root().getAttribute("width") as String
        val value = raw.replace("px", "")
        return value.toInt()
    }

    fun scaleUp(factor: Double = 0.1) {
        var f = factor
        if (factor < 1) f = 1 + factor
        val oldHeight = getHeight()
        val oldWidth = getWidth()
        setHeight((oldHeight * f).toInt())
        setWidth((oldWidth * f).toInt())
    }

    fun scaleDown(factor: Double = 0.1) {
        scaleUp(factor * -1)
    }

}
