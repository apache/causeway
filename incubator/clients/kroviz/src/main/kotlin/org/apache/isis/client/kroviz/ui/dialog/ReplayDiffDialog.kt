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

import io.kvision.core.CssSize
import io.kvision.core.UNIT
import io.kvision.panel.Direction
import io.kvision.panel.SplitPanel
import io.kvision.panel.VPanel
import org.apache.isis.client.kroviz.ui.core.FormItem
import org.apache.isis.client.kroviz.ui.core.RoDialog

class ReplayDiffDialog : Command() {
    var dialog: RoDialog

    val expectedPanel = VPanel(spacing = 3) {
        width = CssSize(20, UNIT.perc)
    }
    val actualPanel = VPanel(spacing = 3) {
        width = CssSize(80, UNIT.perc)
    }

    init {
        dialog = RoDialog(
            caption = "Replay Diff",
            items = mutableListOf<FormItem>(),
            command = this,
            defaultAction = "",
            widthPerc = 60,
            customButtons = mutableListOf<FormItem>()
        )
        val splitPanel = SplitPanel(direction = Direction.VERTICAL)
        splitPanel.add(expectedPanel)
        splitPanel.add(actualPanel)
        dialog.formPanel!!.add(splitPanel)
    }

}
