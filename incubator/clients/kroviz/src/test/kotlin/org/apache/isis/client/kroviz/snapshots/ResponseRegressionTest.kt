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

/**
 * This is an integration test that requires <Demo> running on http://localhost:8080
 * automate via -> @link https://bmuschko.com/blog/docker-integration-testing/
 * compare json -> @link https://stackoverflow.com/questions/26049303/how-to-compare-two-json-have-the-same-properties-without-order
 * eventually use HttpClient? -> @link https://blog.kotlin-academy.com/how-to-create-a-rest-api-client-and-its-integration-tests-in-kotlin-multiplatform-d76c9a1be348
 */
class ResponseRegressionTest {

    //TODO
    /*
    @BeforeTest
    fun setup() {
        val user = "sven"
        val pw = "pass"
        val url = "http://${user}:${pw}@localhost:8080/restful/"
        SessionManager.login(url, user, pw)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun testCompareSnapshotWithResponse() {
        //given
        val map = Response2Handler.map
        val credentials = SessionManager.getCredentials()
        //when
        console.log("[RRT.testCompareSnapshotWithResponse]")
        map.forEach { rh ->

            val handler = rh.value
            val jsonStr = rh.key.str
            val expected = handler.parse(jsonStr)

            val href = rh.key.url
            console.log(href)

            val link = Link(method = Method.GET.operation, href = href)
            val response = invoke(link, credentials)
            val actual = handler.parse(response)
            assertEquals(expected, actual)
            console.log("[RRT.testCompareSnapshotWithResponse]")
            console.log(expected == actual)

        }
        //then
        assertTrue(true, "no exception in loop")
    }

    @ExperimentalCoroutinesApi
    private fun invoke(link: Link, credentials: String): String {
        return "TODO"
//        return TestRequest().fetch(link, credentials)
    }

    private fun invokeSync(link: Link, credentials: String): String {
      //  val hc = Client("id", link.href)
        return "Why must this be so complicated?"
    }
*/
}
