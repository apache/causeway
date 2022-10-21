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

import org.apache.causeway.client.kroviz.handler.TObjectHandler
import org.apache.causeway.client.kroviz.snapshots.demo2_0_0.JAVA_LANG_STRING_JDO
import org.apache.causeway.client.kroviz.snapshots.simpleapp1_16_0.CFG_1
import org.apache.causeway.client.kroviz.snapshots.simpleapp1_16_0.SO_0
import org.apache.causeway.client.kroviz.to.TObject
import org.apache.causeway.client.kroviz.to.Value
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class ExposerTest {

    @Test
    fun testParseJavaLangStringJdo() {
        //given
        val jsonStr = JAVA_LANG_STRING_JDO.str
        //when
        val to = TObjectHandler().parse(jsonStr) as TObject
        //then
        assertEquals("StringJDO entity: Hello", to.title)
        assertEquals("demo.JavaLangStringJdo", to.domainType)
        assertEquals("1", to.instanceId)
    }


    @Test
    fun testConfiguration() {
        val jsonStr = CFG_1.str
        val to = TObjectHandler().parse(jsonStr) as TObject

        val exposer = Exposer(to).dynamise()

        val actKey = exposer["key"]
        assertEquals("causeway.appManifest", actKey)

        val actValue = exposer["value"]
        assertEquals("domainapp.application.manifest.DomainAppAppManifest", actValue)

        val members = to.members
        assertFalse(members.isEmpty())
    }

    @Test
    fun testSimpleObject() {
        val jsonStr = SO_0.str
        val to = TObjectHandler().parse(jsonStr) as TObject
        val exposer = Exposer(to)

        //TODO datanucleus may be gone with Apache Causeway 2.0.x
        val actualDnId = exposer.get("datanucleusIdLong") as Value
        assertEquals(0, actualDnId.content as Long)

        val actualDnvers = exposer.get("datanucleusVersionTimestamp") as Value
        assertEquals(1514897074953L, actualDnvers.content as Long)

        val dynEx = exposer.dynamise()
        val actKey = dynEx["name"]
        assertEquals("Foo", actKey)

        val actValue = dynEx["notes"]
        assertEquals("", actValue)

        val members = to.members
        assertFalse(members.isEmpty())
    }

}
