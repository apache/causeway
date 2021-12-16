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
package org.apache.isis.client.kroviz

import io.kvision.*
import io.kvision.pace.Pace
import io.kvision.panel.VPanel
import io.kvision.panel.root
import org.apache.isis.client.kroviz.ui.core.RoApp
import org.apache.isis.client.kroviz.ui.core.ViewManager

class App : Application() {

    var roApp : RoApp? = null

    init {
        Pace.init()
        require("css/kroviz.css")
        initRoApp()
    }

    override fun start() {
        val r = root("kroviz", addRow = true)
        val v = VPanel()
        v.add(roApp!!)
        r.add(v)
    }

    override fun dispose(): Map<String, Any> {
        return mapOf()
    }

    fun initRoApp() {
        roApp = RoApp()
        ViewManager.app = this
    }

}

fun main() {
    startApplication(
        ::App,
        module.hot,
        BootstrapModule,
        BootstrapCssModule,
        FontAwesomeModule,
        BootstrapSelectModule,
        BootstrapDatetimeModule,
        BootstrapSpinnerModule,
        BootstrapTypeaheadModule,
        BootstrapUploadModule,
        RichTextModule,
        ChartModule,
        TabulatorModule,
        CoreModule,
        panelsCompatibilityMode = true,
    )
}
