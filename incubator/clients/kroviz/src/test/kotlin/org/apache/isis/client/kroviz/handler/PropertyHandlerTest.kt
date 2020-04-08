package org.apache.isis.client.kroviz.handler;

import kotlinx.serialization.UnstableDefault
import org.apache.isis.client.kroviz.snapshots.simpleapp1_16_0.SO_PROPERTY
import org.apache.isis.client.kroviz.to.Property
import kotlin.test.Test
import kotlin.test.assertNotNull

@UnstableDefault
class PropertyHandlerTest {

    @Test
    fun testParse() {
        val jsonStr = SO_PROPERTY.str
        val p = PropertyHandler().parse(jsonStr) as Property
        assertNotNull(p)
    }

}
