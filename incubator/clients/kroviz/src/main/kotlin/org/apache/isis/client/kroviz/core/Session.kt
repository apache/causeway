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
package org.apache.isis.client.kroviz.core

import org.apache.isis.client.kroviz.core.event.EventStore
import org.apache.isis.client.kroviz.ui.core.UiManager
import org.apache.isis.client.kroviz.utils.StringUtils

/**
 * Keep track of connected server.
 */
class Session {
    private var user: String = ""
    private var pw: String = ""
    var baseUrl: String = ""
    val eventStore = EventStore()

    fun login(url: String, user: String, pw: String) {
        this.user = user
        this.pw = pw
        this.baseUrl = url
        UiManager.updateUser(user)
    }

    fun getCredentials(): String {
        return StringUtils.base64encoded("$user:$pw")
    }

}
