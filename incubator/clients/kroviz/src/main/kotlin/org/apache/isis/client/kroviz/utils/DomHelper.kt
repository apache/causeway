package org.apache.isis.client.kroviz.utils

import org.w3c.dom.Element
import kotlin.browser.document
import kotlin.random.Random

external fun encodeURIComponent(encodedURI: String): String

/**
 * Dom ^= Document Object Model
 */
object DomHelper {

    fun appendTo(response: String, elementId: String) {
        val svgDoc = ScalableVectorGraphic(response).document
        val element = getById(elementId)
        element.asDynamic().appendChild(svgDoc.documentElement)
    }

    fun replaceWith(elementId: String, newImage: ScalableVectorGraphic) {
        val svgDoc = newImage.document
        val element = getById(elementId)!!
        element.firstChild?.let { element.removeChild(it) }
        element.asDynamic().appendChild(svgDoc.documentElement)
    }

    fun download(filename: String, text: String) {
        val element = document.createElement("a")
        element.setAttribute("href", "data:text/plain;charset=utf-8," + encodeURIComponent(text))
        element.setAttribute("download", filename)
        document.body?.appendChild(element)
        element.asDynamic().click()
        document.body?.removeChild(element)
    }

    fun getById(elementId: String): Element? {
        return document.getElementById(elementId)
    }

    @ExperimentalUnsignedTypes
    // Returns a 36-character string in the form
    // XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX
    // 12345678 9012 3456 7890 123456789012
    fun uuid(): String {
        val abData = Random.Default.nextBytes(16)
        abData[6] = (0x40 or (abData[6].toInt() and 0xf)).toByte()
        abData[8] = (0x80 or (abData[8].toInt() and 0x3f)).toByte()

        var strHex = abData.toHexString()
        strHex = "$(strHex.substring(0, 8))-$(strHex.substring(8, 4))-$(strHex.substring(12, 4))-$(strHex.substring(16, 4))-$(strHex.substring(20, 12))"
        return strHex
    }

    @ExperimentalUnsignedTypes // just to make it clear that the experimental unsigned types are used
    fun ByteArray.toHexString() = asUByteArray().joinToString("") {
        it.toString(16).padStart(2, '0')
    }

}
