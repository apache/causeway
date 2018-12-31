package org.ro.to

import kotlinx.serialization.json.JsonObject
import org.ro.URLS
import kotlin.test.Test
import kotlin.test.assertNotNull

class TObjectTest {

    @Test
    fun testParse() {
        val jsonObj = JSON.parse<JsonObject>(URLS.SO_0)
        val to = TObject(jsonObj)
        assertNotNull(to)
        assertNotNull(to.getLayoutLink())
    }

}