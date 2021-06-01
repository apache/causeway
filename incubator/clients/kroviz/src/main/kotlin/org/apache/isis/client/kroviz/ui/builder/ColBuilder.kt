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
package org.apache.isis.client.kroviz.ui.builder

import io.kvision.core.*
import io.kvision.panel.FieldsetPanel
import io.kvision.panel.FlexPanel
import io.kvision.panel.HPanel
import org.apache.isis.client.kroviz.to.TObject
import org.apache.isis.client.kroviz.to.bs3.Col
import org.apache.isis.client.kroviz.ui.core.MenuFactory
import org.apache.isis.client.kroviz.ui.core.RoDisplay

class ColBuilder : UiBuilder() {

    fun create(col: Col, tObject: TObject, dsp: RoDisplay): FlexPanel {
        val panel = buildPanel()
        console.log("[CB.create] col:")
        console.log(col)

        if (col.actionList.size > 0) {
            val menu = createMenu(tObject, dsp)
            panel.add(menu)
        }

        for (tg in col.tabGroupList) {
            val tgCpt = TabGroupBuilder().create(tg, tObject, dsp)
            panel.add(tgCpt)
        }
        for (fs in col.fieldSetList) {
            val fsCpt = FieldSetBuilder().create(fs, tObject, dsp)!!
            val fsPanel = FieldsetPanel(legend = fs.name).add(fsCpt)
            panel.add(fsPanel)
        }
        return panel
    }

    private fun buildPanel(): FlexPanel {
        return FlexPanel(
                FlexDirection.COLUMN,
                FlexWrap.NOWRAP,
                JustifyContent.SPACEBETWEEN,
                AlignItems.CENTER,
                AlignContent.STRETCH,
                spacing = 10)
    }

    fun createMenu(tObject: TObject, dsp: RoDisplay): HPanel {
        val panel = HPanel()
        style(panel)

        val dd = MenuFactory.buildForObject(tObject)
        dd.marginTop = CssSize(10, UNIT.px)
        dd.marginBottom = CssSize(10, UNIT.px)
        dd.width = CssSize(100, UNIT.perc)
        MenuFactory.amendWithSaveUndo(dd, tObject)
        MenuFactory.disableSaveUndo(dd)
        dsp.menu = dd
        panel.add(dd)

        return panel
    }


}
