package org.apache.isis.client.kroviz.utils

import org.w3c.dom.parsing.DOMParser
import kotlin.browser.document

external fun encodeURIComponent(encodedURI: String): String

/**
 * Dom ^= Document Object Model
 */
object DomHelper {

    fun appendTo(response: String, elementId: String, type: String = "image/svg+xml") {
        val p = DOMParser()
        val svg = p.parseFromString(response, type)
        val element = document.getElementById(elementId)
        element.asDynamic().appendChild(svg.documentElement)
    }

    fun download(filename: String, text: String) {
        val element = document.createElement("a")
        element.setAttribute("href", "data:text/plain;charset=utf-8," + encodeURIComponent(text))
        element.setAttribute("download", filename)
        document.body?.appendChild(element)
        element.asDynamic().click()
        document.body?.removeChild(element)
    }

}
