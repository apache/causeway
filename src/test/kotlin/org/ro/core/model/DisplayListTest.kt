package org.ro.core.model

import kotlinx.serialization.UnstableDefault
import org.ro.handler.LayoutHandler
import org.ro.handler.TObjectHandler
import org.ro.layout.Layout
import org.ro.to.TObject
import org.ro.urls.SO_0
import org.ro.urls.SO_1
import org.ro.urls.SO_OBJECT_LAYOUT
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@UnstableDefault
class DisplayListTest {

    @Test
    fun testParse() {
        val ro0 = TObjectHandler().parse(SO_0.str) as TObject
        val ro1 = TObjectHandler().parse(SO_1.str) as TObject
        val lyt = LayoutHandler().parse(SO_OBJECT_LAYOUT.str) as Layout

        val ol = DisplayList("test")
        ol.addData(ro0)
        ol.addData(ro1)

        ol.layout = lyt
        assertEquals(2, ol.getData().size)

        assertNotNull(ol.layout)
        assertNotNull(ol.layout!!.properties)
    }

}
