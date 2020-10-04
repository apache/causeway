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
import org.apache.isis.client.kroviz.to.bs3.Grid
import org.apache.isis.client.kroviz.ui.kv.RoDisplay
import pl.treksoft.kvision.core.CssSize
import pl.treksoft.kvision.core.UNIT
import pl.treksoft.kvision.panel.VPanel

class LayoutBuilder {

    fun create(grid: Grid, tObject: TObject, dsp: RoDisplay): VPanel {
        val result = VPanel()

        val oCpt = RowBuilder().createMenu(tObject, dsp)
        oCpt.width = CssSize(100, UNIT.perc)
        result.add(oCpt)

        for (rl in grid.rows) {
            val cpt = RowBuilder().create(rl, tObject, dsp)
            result.add(cpt)
        }
        return result
    }

}

