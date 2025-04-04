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
package org.apache.causeway.client.kroviz.ui.builder

import io.kvision.core.*
import io.kvision.dropdown.DropDown
import io.kvision.panel.VPanel
import org.apache.causeway.client.kroviz.core.model.ObjectDM
import org.apache.causeway.client.kroviz.core.model.ObjectLayout
import org.apache.causeway.client.kroviz.to.TObject
import org.apache.causeway.client.kroviz.ui.core.Displayable
import org.apache.causeway.client.kroviz.ui.menu.DropDownMenuBuilder

class RoDisplay(val displayModel: ObjectDM) : Displayable, VPanel() {

    var menu: DropDown? = null
    private var objectPanel: VPanel

    init {
        val model = displayModel.data!!
        val tObject: TObject = model.delegate
        val grid = (displayModel.layout as ObjectLayout).grid
        objectPanel = LayoutBuilder().create(grid, tObject, this)
        objectPanel.overflow = Overflow.AUTO
        objectPanel.width = CssSize(100, UNIT.perc)
        add(objectPanel)
    }

    override fun setDirty(value: Boolean) {
        displayModel.setDirty(value)
        if (value) {
            this.fontStyle = FontStyle.ITALIC
            this.fontWeight = FontWeight.BOLD
            if (menu != null) {
                DropDownMenuBuilder().enableSaveUndo(menu!!)
            }
        } else {
            this.fontStyle = FontStyle.NORMAL
            this.fontWeight = FontWeight.NORMAL
            if (menu != null) {
                DropDownMenuBuilder().disableSaveUndo(menu!!)
            }
        }
    }

}
