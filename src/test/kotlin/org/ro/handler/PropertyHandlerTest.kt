package org.ro.handler;

import kotlinx.serialization.UnstableDefault
import org.ro.to.Property
import org.ro.snapshots.ai1_16_0.SO_PROPERTY
import kotlin.test.Test
import kotlin.test.assertNotNull

@UnstableDefault
public class PropertyHandlerTest {

    @Test
    fun testParse() {
        val jsonStr = SO_PROPERTY.str
        val p = PropertyHandler().parse(jsonStr) as Property
        assertNotNull(p)
    }

}
