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
package org.apache.causeway.client.kroviz.to

import org.apache.causeway.client.kroviz.handler.DomainTypeHandler
import org.apache.causeway.client.kroviz.snapshots.demo2_0_0.FILE_NODE
import org.apache.causeway.client.kroviz.snapshots.demo2_0_0.JAVA_LANG_STRING_ENTITY
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class DomainTypeTest {

    @Test
    fun testParseJavaLangStringEntity() {
        //given
        val jsonStr = JAVA_LANG_STRING_ENTITY.str
        //when
        val to = DomainTypeHandler().parse(jsonStr) as DomainType
        //then
        assertEquals("Java Lang String Jdo", to.extensions.getFriendlyName())
    }

    @Test
    fun testParseFileNode() {
        // given
        val jsonStr = FILE_NODE.str
        // when
        val domainType = DomainTypeHandler().parse(jsonStr) as DomainType
        // then
        val linkList = domainType.links
        assertEquals(2, linkList.size)

        assertEquals("demoapp.dom.domain.properties.PropertyLayout.navigable.FileNodeVm", domainType.canonicalName)

        val members = domainType.members
        assertEquals(19, members.size)

        val typeActions = domainType.typeActions
        assertEquals(2, typeActions.size)

        assertNotNull(domainType.extensions)
    }

}
