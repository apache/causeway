package org.ro.core.model

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

class ObjectListTest {

    @Test
    fun testParse() {
        var jsonStr = SO_0.str
        val ro0 = TObjectHandler().parse(jsonStr) as TObject
        ro0.addMembersAsProperties()

        jsonStr = SO_1.str
        val ro1 = TObjectHandler().parse(jsonStr) as TObject
        ro1.addMembersAsProperties()

        jsonStr = SO_OBJECT_LAYOUT.str
        val lyt = LayoutHandler().parse(jsonStr) as Layout

        val ol = ObjectList()
        ol.list.add(ObjectAdapter(ro0))
        ol.list.add(ObjectAdapter(ro1))

        ol.layout = lyt
        assertEquals(2, ol.list.size)

        assertNotNull(ol.layout)
        assertNotNull(ol.layout!!.properties)
    }

}