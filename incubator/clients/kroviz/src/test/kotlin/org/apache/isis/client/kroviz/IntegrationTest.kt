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

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.apache.isis.client.kroviz.core.aggregator.BaseAggregator
import org.apache.isis.client.kroviz.core.event.EventStore
import org.apache.isis.client.kroviz.core.event.LogEntry
import org.apache.isis.client.kroviz.core.event.ResourceSpecification
import org.apache.isis.client.kroviz.handler.ResponseHandler
import org.apache.isis.client.kroviz.snapshots.Response
import org.apache.isis.client.kroviz.to.Method
import org.apache.isis.client.kroviz.ui.core.Constants
import org.apache.isis.client.kroviz.ui.core.SessionManager
import org.apache.isis.client.kroviz.utils.XmlHelper
import org.w3c.xhr.XMLHttpRequest

// subclasses expect a running backend, here SimpleApp localhost:8080/restful*

open class IntegrationTest {

    fun isAppAvailable(): Boolean {
        val user = "sven"
        val pw = "pass"
        val url = "http://${user}:${pw}@localhost:8080/restful/"
        SessionManager.login(url, user, pw)
        val credentials: String = SessionManager.getCredentials()!!
        val xhr = XMLHttpRequest()
        xhr.open("GET", url, false, user, pw)
        xhr.setRequestHeader("Authorization", "Basic $credentials")
        xhr.setRequestHeader("Content-Type", "application/json;charset=UTF-8")
        xhr.setRequestHeader("Accept", "application/json")

        try {
            xhr.send() // there will be a 'pause' here until the response comes.
        } catch (e: Throwable) {
            return false
        } finally {

        }
        val answer = xhr.status.equals(200)
        return answer
    }

    fun mockResponse(response: Response, aggregator: BaseAggregator?): LogEntry {
        val str = response.str
        val subType = when (XmlHelper.isXml(str)) {
            true -> Constants.subTypeXml
            else -> Constants.subTypeJson
        }
        val reSpec = ResourceSpecification(response.url, subType)
        val es = EventStore()
        es.start(
                reSpec,
                Method.GET.operation,
                "",
                aggregator)
        val le = es.end(reSpec, str)!!
        ResponseHandler.handle(le)
        wait(100)
        return le
    }

    fun wait(milliseconds: Long) {
        GlobalScope.launch {
            delay(milliseconds)
        }
    }

}
