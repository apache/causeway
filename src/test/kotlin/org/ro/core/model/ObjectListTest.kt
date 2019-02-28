package org.ro.core.model


import org.ro.handler.LayoutHandler
import org.ro.handler.TObjectHandler
import org.ro.to.SO_0
import org.ro.to.SO_1
import org.ro.to.SO_OBJECT_LAYOUT
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


class ObjectListTest {

    @Test
    fun testParse() {
        var jsonStr = SO_0.str
        val ro0 = TObjectHandler().parse(jsonStr)
        ro0.addMembersAsProperties()

        jsonStr = SO_1.str
        val ro1 = TObjectHandler().parse(jsonStr)
        ro1.addMembersAsProperties()

        jsonStr = SO_OBJECT_LAYOUT.str
        val lyt = LayoutHandler().parse(jsonStr)

        val ol = ObjectList()
        ol.initSize(2)

        val oa0 = ObjectAdapter(ro0)
        ol.add(oa0)

        val oa1 = ObjectAdapter(ro1)
        ol.add(oa1)

        ol.setLayout(lyt)
        assertEquals(2, ol.length())

        assertNotNull(ol.getLayout())
        assertNotNull(ol.getLayout()!!.properties)
    }

}