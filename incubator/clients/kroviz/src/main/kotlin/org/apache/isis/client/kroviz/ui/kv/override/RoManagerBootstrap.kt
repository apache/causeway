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
package org.apache.isis.client.kroviz.ui.kv.override

/* (!) copied from kvision KVManagerBootstrap in order to make Dialogs transparent on move */

import io.kvision.core.Component
import io.kvision.utils.isIE11

internal val roManagerBootstrapInit = RoManagerBootstrap.init()

/**
 * Internal singleton object which initializes and configures KVision Bootstrap module.
 */
internal object RoManagerBootstrap {
    init {
        io.kvision.require("bootstrap/dist/js/bootstrap.bundle.min.js")
        io.kvision.require("awesome-bootstrap-checkbox")
    }

    private val elementResizeEvent = io.kvision.require("element-resize-event")

    @Suppress("UnsafeCastFromDynamic")
    internal fun setResizeEvent(component: Component, callback: () -> Unit) {
        if (!isIE11()) {
            component.getElement()?.let {
                elementResizeEvent(it, callback)
            }
        }
    }

    @Suppress("UnsafeCastFromDynamic")
    internal fun clearResizeEvent(component: Component) {
        if (!isIE11()) {
            if (component.getElement()?.asDynamic()?.__resizeTrigger__?.contentDocument != null) {
                component.getElement()?.let {
                    elementResizeEvent.unbind(it)
                }
            }
        }
    }

    internal fun init() {}
}
