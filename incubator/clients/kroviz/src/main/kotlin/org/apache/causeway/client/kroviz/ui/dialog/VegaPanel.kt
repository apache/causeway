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
package org.apache.causeway.client.kroviz.ui.dialog

import io.kvision.panel.SimplePanel
import io.kvision.utils.obj
import org.apache.causeway.client.kroviz.to.VEGA_SAMPLE
import org.apache.causeway.client.kroviz.to.Vega5
import org.apache.causeway.client.kroviz.utils.js.Vega

class VegaPanel : SimplePanel() {

    // https://vega.github.io/vega/examples/

    init {
        this.addAfterInsertHook {
            val json = VEGA_SAMPLE.str
            val specFromClass = JSON.parse<Vega5>(json)
            console.log("[VP#init]")
            console.log(specFromClass)
            val view = Vega.View(Vega.parse(specFromClass), obj {
                this.renderer = "canvas"
                this.container = getElement()
                this.hover = true
            })
            console.log(view)
            view.runAsync()
        }
    }

}
