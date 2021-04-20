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

import org.apache.isis.client.kroviz.to.TObject
import org.apache.isis.client.kroviz.to.bs3.Row
import org.apache.isis.client.kroviz.ui.kv.MenuFactory
import org.apache.isis.client.kroviz.ui.kv.RoDisplay
import io.kvision.core.*
import io.kvision.core.FlexWrap
import io.kvision.panel.*

class RowBuilder {

    fun create(row: Row, tObject: TObject, dsp: RoDisplay): SimplePanel {
        val result = FlexPanel(
                FlexDirection.ROW,
                FlexWrap.NOWRAP,
                JustifyContent.FLEXSTART,
                AlignItems.FLEXSTART,
                AlignContent.STRETCH,
                spacing = 10 )

        for (c in row.colList) {
            val cpt = ColBuilder().create(c, tObject, dsp)
            result.add(cpt)
        }
        return result
    }

    fun createMenu(tObject: TObject, dsp: RoDisplay): HPanel {
        val result = HPanel()
        result.width = CssSize(100, UNIT.perc)
        result.height = CssSize(100, UNIT.perc)

        val dd = MenuFactory.buildForObject(tObject)
        dd.marginTop = CssSize(10, UNIT.px)
        dd.marginBottom = CssSize(10, UNIT.px)
        MenuFactory.amendWithSaveUndo(dd, tObject)
        MenuFactory.disableSaveUndo(dd)
        dsp.menu = dd
        result.add(dd)

        return result
    }

}
