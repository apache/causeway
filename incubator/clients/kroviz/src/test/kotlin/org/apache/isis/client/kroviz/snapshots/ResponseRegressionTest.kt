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
package org.apache.isis.client.kroviz.snapshots

import org.apache.isis.client.kroviz.snapshots.demo2_0_0.Response2Handler
import org.apache.isis.client.kroviz.to.Link
import org.apache.isis.client.kroviz.to.Method
import org.apache.isis.client.kroviz.ui.kv.UiManager
import pl.treksoft.kvision.require
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * This is an integration test that requires <Demo> running on http://localhost:8080
 * automate via -> @link https://bmuschko.com/blog/docker-integration-testing/
 * compare json -> @link https://stackoverflow.com/questions/26049303/how-to-compare-two-json-have-the-same-properties-without-order
 */
class ResponseRegressionTest {

    @BeforeTest
    fun setup() {
//        require("xmlhttprequest").XmlHttpRequest;
        val user = "sven"
        val pw = "pass"
        val url = "http://${user}:${pw}@localhost:8080/restful/"
        UiManager.login(url, user, pw)
    }

    //@Test  // invoking HttpRequest does not work - yet
    fun testCompareSnapshotWithResponse() {
        //given
        val map = Response2Handler.map
        val credentials = UiManager.getCredentials()
        //when
        console.log("[RRT.testCompareSnapshotWithResponse]")
        map.forEach { rh ->
            val handler = rh.value
            val jsonStr = rh.key.str
            val expected = handler.parse(jsonStr)

            val href = rh.key.url
            console.log(href)

            val link = Link(method = Method.GET.operation, href = href)
            val response = SyncRequest().invoke(link, credentials)

            val actual = handler.parse(response)

            assertEquals(expected, actual)
        }
        //then
        assertTrue(true, "no exception in loop")
    }

}
