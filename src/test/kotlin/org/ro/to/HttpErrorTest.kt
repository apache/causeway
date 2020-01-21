package org.ro.to

import kotlinx.serialization.UnstableDefault
import org.ro.handler.HttpErrorHandler
import org.ro.snapshots.simpleapp1_16_0.HTTP_ERROR
import org.ro.snapshots.simpleapp1_16_0.HTTP_ERROR_500_UNIQUE_CONSTRAINT_VIOLATION
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@UnstableDefault
class HttpErrorTest {

    @Test
    fun test400() {
        val jsonStr = HTTP_ERROR.str
        val error = HttpErrorHandler().parse(jsonStr)  as HttpError
        val code = error.httpStatusCode
        assertEquals(400, code)
        assertNotNull(error.message)

        val detail = error.detail
        assertNotNull(detail)
        assertNotNull(detail.className)
        assertNotNull(detail.message)
        assertEquals(error.message, detail.message)
        assertNotNull(detail.element)
        assertTrue(detail.element.size > 0)
    }

    //@Test //TODO handle nested causedBy's
    fun test500() {
        val jsonStr = HTTP_ERROR_500_UNIQUE_CONSTRAINT_VIOLATION.str
        val error = HttpErrorHandler().parse(jsonStr)  as HttpError
        val code = error.httpStatusCode
        assertEquals(400, code)
        assertNotNull(error.message)

        val detail = error.detail
        assertNotNull(detail)
        assertNotNull(detail.className)
        assertNotNull(detail.message)
        assertEquals(error.message, detail.message)
        assertNotNull(detail.element)
        assertTrue(detail.element.size > 0)
    }

}
