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
package org.apache.isis.client.kroviz.ui.core

import org.apache.isis.client.kroviz.core.Session
import org.apache.isis.client.kroviz.core.event.EventStore

/**
 * Single point of contact for view components consisting of:
 * @item Session
 */
object SessionManager {

    private val sessions = mutableListOf<Session>()

    fun getSession(): Session {
        return sessions.first()
    }

    fun getBaseUrl(): String {
        val s = getSession()
        return when (s) {
            null -> ""
            else -> s.baseUrl
        }
    }

    fun getEventStore(): EventStore {
        return getSession().eventStore
    }

    fun login(url: String, username: String, password: String) {
        val s = Session()
        s.login(url, username, password)
        sessions.add(0, s)
        UiManager.updateUser(username)
    }

    fun getCredentials(): String {
        return getSession().getCredentials()
    }

}
