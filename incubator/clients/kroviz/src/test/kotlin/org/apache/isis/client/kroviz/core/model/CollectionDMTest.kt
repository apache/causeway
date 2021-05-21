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
package org.apache.isis.client.kroviz.core.model

import org.apache.isis.client.kroviz.handler.LayoutHandler
import org.apache.isis.client.kroviz.handler.LayoutXmlHandler
import org.apache.isis.client.kroviz.handler.TObjectHandler
import org.apache.isis.client.kroviz.layout.Layout
import org.apache.isis.client.kroviz.snapshots.simpleapp1_16_0.*
import org.apache.isis.client.kroviz.to.TObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class CollectionDMTest {

    @Test
    fun testSimpleObject() {
        val ro0 = TObjectHandler().parse(SO_0.str) as TObject
        val ro1 = TObjectHandler().parse(SO_1.str) as TObject
        val lt = LayoutHandler().parse(SO_OBJECT_LAYOUT.str) as Layout

        val dl = CollectionDM("test")
        dl.addData(ro0)
        dl.addData(ro1)
        dl.addLayout(lt)

        assertEquals(2, dl.data.size) //1

        val properties = dl.propertyLayoutList
        assertEquals(5, properties.size) //2    // includes datanucleus IdLong, VersionLong, VersionTimestamp
        assertEquals("name", properties[0].id) //3
        assertEquals("notes", properties[1].id)  //4
    }

    @Test
    fun testConfiguration() {
        val ro0 = TObjectHandler().parse(CFG_1.str) as TObject
        val lt = LayoutHandler().parse(CFG_LAYOUT_JSON.str) as Layout
        // val grd = LayoutXmlHandler().parse(CFG_LAYOUT_XML.str) as Grid

        val dl = CollectionDM("test")
        dl.addData(ro0)

        dl.addLayout(lt)
        assertEquals(1, dl.data.size) //1

        val properties = dl.propertyLayoutList
        assertEquals("key", properties[0].id) // 2
        assertEquals("value", properties[1].id)   // 3
    }

    //@Test
    fun testFixtureResult() {
        val ro0 = TObjectHandler().parse(FR_OBJECT.str) as TObject
        val lt = LayoutXmlHandler().parse(FR_OBJECT_LAYOUT.str) as Layout

        val dl = CollectionDM("test")
        dl.addData(ro0)

        dl.addLayout(lt)
        assertEquals(1, dl.data.size)

        assertNotNull(dl.layout)
        val properties = dl.propertyList
        assertNotNull(properties)
        //Sequence in FR_OBJECT differs from sequence in FR_OBJECT_LAYOUT
        // FR_OBJECT: fixtureScriptClassName, key, object, className
        // FR_OBJECT_LAYOUT: className, fixtureScriptClassName, key, object
        assertEquals("fixtureScriptClassName", properties[1].id)
        assertEquals("key", properties[2].id)
        assertEquals("object", properties[3].id)
        assertEquals("className", properties[0].id)
    }

}
