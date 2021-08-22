/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.isis.client.kroviz.ui.panel

import io.kvision.form.upload.uploadInput
import io.kvision.html.button
import io.kvision.panel.VPanel
import io.kvision.panel.vPanel
import org.w3c.files.FileReader

@Deprecated("Useful as Sample")
object ImageSample : VPanel() {


    init {
        vPanel(spacing = 10) {
            val button = button("Button")
            val upload = uploadInput("/") {
                showUpload = false
                showCancel = false
            }
            button("Add image to button").onClick {
                upload.value?.firstOrNull()?.let { upload.getNativeFile(it) }?.slice()?.let { blob ->
                    //
                    // Important part is here
                    //
                    val reader = FileReader()
                    reader.addEventListener("load", {
                        val dataUrl = it.target.asDynamic().result
                        console.log("[IS.init]")
                        console.log(dataUrl)
                        button.image = dataUrl
                    })
                    reader.readAsDataURL(blob)
                }
            }
        }
    }


}
