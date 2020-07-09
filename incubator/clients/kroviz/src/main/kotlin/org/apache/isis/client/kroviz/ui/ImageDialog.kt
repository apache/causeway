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
package org.apache.isis.client.kroviz.ui

import org.apache.isis.client.kroviz.to.ValueType
import org.apache.isis.client.kroviz.ui.kv.RoDialog
import org.apache.isis.client.kroviz.utils.*

class ImageDialog(
        var label: String,
        private var pumlCode: String) : Command() {

    private var callBack: Any = UUID()
    private var dialog: RoDialog
    private val formItems = mutableListOf<FormItem>()

    fun open() {
        dialog.open()
        UmlUtils.generateDiagram(pumlCode, callBack)
    }

    init {
        val fi = FormItem("svg", ValueType.IMAGE.type, callBack = callBack)
        formItems.add(fi)

        dialog = RoDialog(
                widthPerc = 80,
                caption = "Diagram",
                items = formItems,
                command = this)
        //callBack = fi.callBack!!
    }

    @Deprecated("use leaflet/svg")
    fun scale(direction: Direction) {
        val uuid = callBack as UUID
        val oldElement = DomUtil.getById(uuid.value)!!
        val oldStr = oldElement.innerHTML
        val newImage = ScalableVectorGraphic(oldStr)
        when (direction) {
            Direction.UP -> newImage.scaleUp()
            Direction.DOWN -> newImage.scaleDown()
        }
        DomUtil.replaceWith(uuid, newImage)
    }

}
