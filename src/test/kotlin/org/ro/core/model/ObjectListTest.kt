package org.ro.core.model

import kotlinx.serialization.UnstableDefault
import org.ro.handler.LayoutHandler
import org.ro.handler.TObjectHandler
import org.ro.layout.Layout
import org.ro.to.SO_0
import org.ro.to.SO_1
import org.ro.to.SO_OBJECT_LAYOUT
import org.ro.to.TObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@UnstableDefault
class ObjectListTest {

    @Test
    fun testParse() {
        val ro0 = TObjectHandler().parse(SO_0.str) as TObject
        val ro1 = TObjectHandler().parse(SO_1.str) as TObject
        val lyt = LayoutHandler().parse(SO_OBJECT_LAYOUT.str) as Layout

        val ol = ObjectList()
        ol.list.add(Exposer(ro0))
        ol.list.add(Exposer(ro1))

        ol.layout = lyt
        assertEquals(2, ol.list.size)

        assertNotNull(ol.layout)
        assertNotNull(ol.layout!!.properties)

        ol.initPropertyDescription()
        assertTrue(ol.hasLayout())
    }

}
