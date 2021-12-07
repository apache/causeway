/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.isis.client.kroviz.handler

import io.kvision.html.Image
import kotlinx.browser.window
import org.apache.isis.client.kroviz.IntegrationTest
import org.apache.isis.client.kroviz.core.aggregator.DispatchInterceptor
import org.apache.isis.client.kroviz.core.event.ResourceProxy
import org.apache.isis.client.kroviz.snapshots.demo2_0_0.OBJECT_ICON
import org.apache.isis.client.kroviz.to.Icon
import org.apache.isis.client.kroviz.to.Link
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class IconHandlerTest : IntegrationTest() {

//    @Test
    fun handleTest() {
        if (isAppAvailable()) {
            //given
            val link = Link(href = OBJECT_ICON.url)
            val di = DispatchInterceptor()
            ResourceProxy().fetch(link, di)
            wait(1000)
            val logEntry = di.logEntry!!

            //when
            val handler = IconHandler()
            handler.handle(logEntry)
            val icon = handler.parse("") as Icon
            val image = icon.image

            //then
            assertTrue(image.sizes != "")
            console.log("[IHT.handleTest]")
            console.log(image.sizes)
        }
    }

    //@Test
    fun testInline() {
        val VMT = "\$vmT"
        val str = """
"�PNG


IHDR�w=�sBIT|d�	pHYs��~�tEXtSoftwareAdobe Fireworks CS5q��6_IDATH���i�0�?��7${'$'}��TP�
�N���
���l�����C�Z�bZ�P�c,_�s�����	���@+9"`���/ �${'$'}+�ߴ0ƴ�����ɽ'�w
,���a�{G v${VMT}�6��N ��C_�Mwbұ�J+P�[��Իf��P����B+�
�'��h20IVo��+{��{`�IV�ZI=�Ⱥ\k��߹u�G���${'$'}+�ǡ+_���6�Y�ӡ}>���7���?8;hZ�N'sL�kA+����w+`���h\���]�����iȂ�!�����ֆ���Q�6�.�̨�U�����0b&bs�:���o�맺y��IEND�B`�"
"""
        val ba = ByteArray(str.length)
        str.forEachIndexed { index, char ->
            ba.set(index, char.toByte())
        }
        console.log(ba.toString())
        val options = BlobPropertyBag()
        options.type = "image/png"
        val blob = Blob(ba.asDynamic(), options)
        console.log(ba.size.toString())
        console.log(blob.size.toString())
        assertNotEquals(blob.size, ba.size)

        val url = URL.createObjectURL(blob)
        Image(url)
        console.log(url)
        window.open(url)
    }


}
