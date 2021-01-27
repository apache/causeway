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

import org.apache.isis.client.kroviz.ui.kv.RoApp
import pl.treksoft.kvision.Application
import pl.treksoft.kvision.pace.Pace
import pl.treksoft.kvision.panel.root
import pl.treksoft.kvision.panel.vPanel
import pl.treksoft.kvision.require
import pl.treksoft.kvision.startApplication
import pl.treksoft.kvision.utils.px

class App : Application() {

    init {
        require("css/kroviz.css")
        require("lodash")
    }

    override fun start() {
        Pace.init()
        root("kroviz") {
            vPanel(spacing = 0) {
                padding = 0.px
                add(RoApp)
            }
        }
    }

    override fun dispose(): Map<String, Any> {
        return mapOf()
    }
}

fun main() {
    startApplication(::App)
}
