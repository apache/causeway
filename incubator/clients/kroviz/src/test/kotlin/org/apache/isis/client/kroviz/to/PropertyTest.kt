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
package org.apache.isis.client.kroviz.to

import org.apache.isis.client.kroviz.handler.PropertyHandler
import org.apache.isis.client.kroviz.snapshots.demo2_0_0.DOMAIN_TYPES_PROPERTY
import org.apache.isis.client.kroviz.snapshots.demo2_0_0.PROPERTY
import org.apache.isis.client.kroviz.snapshots.demo2_0_0.PROPERTY_DESCRIPTION
import org.apache.isis.client.kroviz.snapshots.simpleapp1_16_0.FR_OBJECT_PROPERTY_
import org.apache.isis.client.kroviz.snapshots.simpleapp1_16_0.SO_PROPERTY
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PropertyTest {

    @Test
    fun testDemoPropertyDescription() {
        val jsonStr = PROPERTY_DESCRIPTION.str
        val p = PropertyHandler().parse(jsonStr) as Property
        assertEquals("parity", p.id)
        assertEquals("The parity of this 'DemoItem'.", p.extensions!!.getDescription())
    }

    @Test
    fun testDemoObjectProperty() {
        val jsonStr = PROPERTY.str
        val p = PropertyHandler().parse(jsonStr) as Property
        assertEquals("string", p.id)
        assertEquals("string", p.extensions!!.xIsisFormat)
        assertEquals(5, p.links.size)

        val modifyLink = p.links[2]
        assertEquals("PUT", modifyLink.method)

        val arguments = modifyLink.arguments
        assertEquals(1, arguments.size)
        assertTrue(arguments.containsKey("value"))
    }

    @Test
    fun testSimpleObjectProperty() {
        val jsonStr = SO_PROPERTY.str
        val p = PropertyHandler().parse(jsonStr) as Property
        assertEquals("notes", p.id)
        assertEquals("string", p.extensions!!.xIsisFormat)
        assertEquals(5, p.links.size)

        val modifyLink = p.links[2]
        assertEquals("PUT", modifyLink.method)

        val arguments = modifyLink.arguments
        assertEquals(1, arguments.size)
        assertTrue(arguments.containsKey("value"))
    }

    @Test
    fun testFixtureResultObjectPropety() {
        val jsonStr = FR_OBJECT_PROPERTY_.str
        val p = PropertyHandler().parse(jsonStr) as Property
        val actual = p.disabledReason!!
        val expected = "Non-cloneable view models are read-only; Immutable"
        assertEquals(expected, actual)
    }

    @Test
    fun testDemoDomainProperty() {
        val jsonStr = DOMAIN_TYPES_PROPERTY.str
        val p = PropertyHandler().parse(jsonStr) as Property
        val e: Extensions = p.extensions!!
        val actual = e.getFriendlyName()
        val expected = "Read Only Property Derived Render Day Not Specified"
        assertEquals(expected, actual)
    }

}
