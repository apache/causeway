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
package org.apache.causeway.client.kroviz.core.model

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.apache.causeway.client.kroviz.snapshots.simpleapp1_16_0.FR_OBJECT_BAZ
import org.apache.causeway.client.kroviz.to.Link
import org.apache.causeway.client.kroviz.to.Member
import org.apache.causeway.client.kroviz.to.TObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class FixtureResultTest {

//TODO    @Test
    fun testObjectBAZ() {
        // given
        val jsonStr = FR_OBJECT_BAZ.str

        // when
        val tObj = Json.decodeFromString<TObject>(jsonStr)
        val dynObj = tObj.asDynamic()

        // then
        assertNotNull(dynObj)    //1

        val expectedTitle = "domain-app-demo/persist-all/item-3:  Object: Baz"
        val actualTitle: String = dynObj.title
        assertEquals(expectedTitle, actualTitle)  //2

        assertTrue(dynObj.hasOwnProperty("domainType"))    //3
        assertTrue(dynObj.hasOwnProperty("instanceId"))       //4

        //Expectations:
        // 1:  has members (memberList?) mapped onto (dynamic) MemberExposer properties
        assertTrue(dynObj.hasOwnProperty("members"))   //5 only internal (Object) attributes are 'adapted'
        val members = dynObj.members
        val memberMap = members as LinkedHashMap<String, Member>
        assertNotNull(memberMap)              //6
        assertEquals(8, memberMap.size)    //7

        // 3:  has links (linkList?) mapped onto (dynamic) MemberExposer properties
//        assertTrue(dynObj.hasOwnProperty("links"))   //8 only internal (Object) attributes are 'adapted'
        val links = tObj.links
        val linkList = links as ArrayList<Link>?
        assertNotNull(linkList)          //9
        assertEquals(4, linkList.size)  //10
    }

}
