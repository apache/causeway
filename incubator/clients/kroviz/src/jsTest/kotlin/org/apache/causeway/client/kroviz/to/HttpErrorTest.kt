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

import org.apache.causeway.client.kroviz.handler.Http401ErrorHandler
import org.apache.causeway.client.kroviz.handler.HttpErrorHandler
import org.apache.causeway.client.kroviz.snapshots.demo2_0_0.HTTP_ERROR_401
import org.apache.causeway.client.kroviz.snapshots.demo2_0_0.HTTP_ERROR_403
import org.apache.causeway.client.kroviz.snapshots.demo2_0_0.HTTP_ERROR_405
import org.apache.causeway.client.kroviz.snapshots.demo2_0_0.HTTP_ERROR_500
import org.apache.causeway.client.kroviz.snapshots.simpleapp1_16_0.HTTP_ERROR
import org.apache.causeway.client.kroviz.snapshots.simpleapp1_16_0.HTTP_ERROR_500_UNIQUE_CONSTRAINT_VIOLATION
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class HttpErrorTest {

    @Test
    fun testKnife500() {
        val jsonStr = org.apache.causeway.client.kroviz.snapshots.knife.HTTP_ERROR_500.str
        val error = HttpErrorHandler().parse(jsonStr) as HttpError
        val code = error.getStatusCode()
        assertEquals(500, code)
        assertNotNull(error.getMessage())

        val detail = error.detail
        assertNotNull(detail)
        assertNotNull(detail.className)
        assertEquals(null, detail.message)
        assertEquals("", error.getMessage())
        assertNotNull(detail.element)
        assertTrue(detail.element.size > 0)
    }

    @Test
    fun testDemo500() {
        val jsonStr = HTTP_ERROR_500.str
        val error = HttpErrorHandler().parse(jsonStr) as HttpError
        val code = error.getStatusCode()
        assertEquals(500, code)
        assertNotNull(error.getMessage())

        val detail = error.detail
        assertNotNull(detail)
        assertNotNull(detail.className)
        assertNotNull(detail.message)
        assertEquals(error.getMessage(), detail.message)
        assertNotNull(detail.element)
        assertTrue(detail.element.size > 0)
    }

    @Test
    fun test403() {
        val jsonStr = HTTP_ERROR_403.str
        val error = HttpErrorHandler().parse(jsonStr) as HttpError
        val code = error.getStatusCode()
        assertEquals(403, code)
        assertNotNull(error.getMessage())

        val detail = error.detail
        assertNotNull(detail)
        assertNotNull(detail.className)
        assertNotNull(detail.message)
        assertEquals(error.getMessage(), detail.message)
        assertNotNull(detail.element)
        assertTrue(detail.element.size > 0)
    }

    @Test
    fun test405() {
        val jsonStr = HTTP_ERROR_405.str
        val error = HttpErrorHandler().parse(jsonStr) as HttpError
        val code = error.getStatusCode()
        assertEquals(405, code)
        assertNotNull(error.getMessage())

        val detail = error.detail
        assertNotNull(detail)
        assertNotNull(detail.className)
        assertNotNull(detail.message)
        assertEquals(error.getMessage(), detail.message)
        assertNotNull(detail.element)
        assertTrue(detail.element.size > 0)
    }

    @Test
    fun test400() {
        val jsonStr = HTTP_ERROR.str
        val error = HttpErrorHandler().parse(jsonStr) as HttpError
        val code = error.getStatusCode()
        assertEquals(400, code)
        assertNotNull(error.getMessage())

        val detail = error.detail
        assertNotNull(detail)
        assertNotNull(detail.className)
        assertNotNull(detail.message)
        assertEquals(error.getMessage(), detail.message)
        assertNotNull(detail.element)
        assertTrue(detail.element.size > 0)
    }

    @Test
    fun test401() {
        val jsonStr = HTTP_ERROR_401.str
        val error = Http401ErrorHandler().parse(jsonStr) as Http401Error

        assertEquals(401, error.getStatusCode())
        assertTrue(error.getMessage().startsWith("Unauthorized"))
        assertTrue(error.getMessage().contains("/restful/"))
    }

    //@Test //TODO handle nested causedBy's
    fun test500() {
        val jsonStr = HTTP_ERROR_500_UNIQUE_CONSTRAINT_VIOLATION.str
        val error = HttpErrorHandler().parse(jsonStr) as HttpError
        val code = error.getStatusCode()
        assertEquals(400, code)
        assertNotNull(error.getMessage())

        val detail = error.detail
        assertNotNull(detail)
        assertNotNull(detail.className)
        assertNotNull(detail.message)
        assertEquals(error.getMessage(), detail.message)
        assertNotNull(detail.element)
        assertTrue(detail.element.size > 0)
    }

}
