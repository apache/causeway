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
import io.kvision.panel.FlexPanel
import io.kvision.panel.SimplePanel
import org.apache.causeway.client.kroviz.to.TObject
import org.apache.causeway.client.kroviz.to.bs3.Row
import org.apache.causeway.client.kroviz.ui.core.Constants

class RowBuilder : UiBuilder() {

    fun create(row: Row, tObject: TObject, dsp: RoDisplay): SimplePanel {
        val panel = buildPanel()
        panel.justifyContent = JustifyContent.SPACEBETWEEN

        for (c in row.colList) {
            val cpt = ColBuilder().create(c, tObject, dsp)
            panel.add(cpt)
        }
        return panel
    }

    private fun buildPanel(): FlexPanel {
        return FlexPanel(
                FlexDirection.ROW,
                FlexWrap.NOWRAP,
                JustifyContent.FLEXSTART,
                AlignItems.FLEXSTART,
                AlignContent.STRETCH,
                spacing = Constants.spacing)
    }

}
