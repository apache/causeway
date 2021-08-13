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
package org.apache.isis.client.kroviz.handler

import org.apache.isis.client.kroviz.to.Icon
import org.apache.isis.client.kroviz.to.TransferObject
import org.apache.isis.client.kroviz.ui.core.Constants
import org.w3c.dom.Image
import org.w3c.dom.url.URL
import org.w3c.files.Blob

class IconHandler : BaseHandler() {

    override fun canHandle(response: String): Boolean {
        val isObjectIcon = logEntry.url.endsWith("object-icon")
        if (isObjectIcon) {
            return super.canHandle(response)
        }
        return false
    }

    override fun parse(response: String): TransferObject {
        val blob = logEntry.blob!!
        val image = blob.toImage()
        return Icon(image)
    }

    override fun update() {
        logEntry.getAggregator().update(logEntry, Constants.pngMimeType)
    }

    fun Blob.toImage() : Image {
        val url = URL.createObjectURL(this)
        val image = Image()
        image.src = url
        return image
    }

}
