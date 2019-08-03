package org.ro.to

import kotlinx.serialization.UnstableDefault
import org.ro.handler.HttpErrorHandler
import org.ro.urls.HTTP_ERROR
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@UnstableDefault
class HttpErrorTest {
    @Test
    fun testParse() {
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

}
