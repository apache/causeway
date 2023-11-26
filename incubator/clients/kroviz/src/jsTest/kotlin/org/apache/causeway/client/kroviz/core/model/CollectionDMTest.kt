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

import org.apache.causeway.client.kroviz.handler.LayoutXmlHandler
import org.apache.causeway.client.kroviz.handler.TObjectHandler
import org.apache.causeway.client.kroviz.snapshots.simpleapp1_16_0.*
import org.apache.causeway.client.kroviz.to.TObject
import org.apache.causeway.client.kroviz.to.GridBs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class CollectionDMTest {

    @Test
    fun testSimpleObject() {
        val ro0 = TObjectHandler().parse(SO_0.str) as TObject
        val ro1 = TObjectHandler().parse(SO_1.str) as TObject
        val grd = LayoutXmlHandler().parse(CFG_LAYOUT_XML.str) as GridBs

        val cdm = CollectionDM("test")
        cdm.addData(ro0)
        cdm.addData(ro1)
        cdm.setProtoTypeLayout(grd)

        assertEquals(2, cdm.data.size) //1
    }

    @Test
    fun testConfiguration() {
        val ro0 = TObjectHandler().parse(CFG_1.str) as TObject
        val grd = LayoutXmlHandler().parse(CFG_LAYOUT_XML.str) as GridBs

        val dl = CollectionDM("test")
        dl.addData(ro0)

        dl.setProtoTypeLayout(grd)
        assertEquals(1, dl.data.size) //1
    }

    //@Test
    fun testFixtureResult() {
        val ro0 = TObjectHandler().parse(FR_OBJECT.str) as TObject
        val grd = LayoutXmlHandler().parse(FR_OBJECT_LAYOUT.str) as GridBs

        val dl = CollectionDM("test")
        dl.addData(ro0)

        dl.setProtoTypeLayout(grd)
        assertEquals(1, dl.data.size)

        assertNotNull(dl.layout)
//        val properties = dl.layout!!.propertyList
//        assertNotNull(properties)
        //Sequence in FR_OBJECT differs from sequence in FR_OBJECT_LAYOUT
        // FR_OBJECT: fixtureScriptClassName, key, object, className
        // FR_OBJECT_LAYOUT: className, fixtureScriptClassName, key, object
//        assertEquals("fixtureScriptClassName", properties[1].id)
//        assertEquals("key", properties[2].id)
//        assertEquals("object", properties[3].id)
//        assertEquals("className", properties[0].id)
    }

}
