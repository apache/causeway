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

package org.apache.isis.client.kroviz.ui.dialog

import org.apache.isis.client.kroviz.to.ValueType
import org.apache.isis.client.kroviz.ui.core.FormItem
import org.apache.isis.client.kroviz.ui.core.RoDialog
import org.apache.isis.client.kroviz.utils.DomUtil

class DownloadDialog(val fileName:String, val content:String) : Controller() {

    val formItems = mutableListOf<FormItem>()

    override fun open() {
        formItems.add(FormItem("Preview", ValueType.TEXT_AREA, content, 15))
        dialog = RoDialog(caption = "Download: $fileName", items = formItems, controller = this)
        super.open()
    }

    override fun execute(action:String?) {
        DomUtil.download(fileName, content)
    }

}
