/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.client.kroviz.utils

import org.w3c.dom.Document
import org.w3c.dom.Element
import kotlin.browser.document

external fun encodeURIComponent(encodedURI: String): String

/**
 * Dom ^= Document Object Model
 */
object DomUtil {

    fun appendTo(uuid: UUID, response: String) {
        val svgDoc = ScalableVectorGraphic(response).document
        append(uuid, svgDoc, false)
    }

    fun replaceWith(uuid: UUID, newImage: ScalableVectorGraphic) {
        val svgDoc = newImage.document
        append(uuid, svgDoc, true)
    }

    private fun append(uuid: UUID, svgDoc: Document, replace: Boolean) {
        val element = getById(uuid.value)
        if (element != null) {
            if (replace) {
                element.firstChild?.let { element.removeChild(it) }
            }
            element.asDynamic().appendChild(svgDoc.documentElement)
        }
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

}
