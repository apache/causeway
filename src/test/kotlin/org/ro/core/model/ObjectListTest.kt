package org.ro.core.model

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.JSON
import org.ro.layout.Layout
import org.ro.to.SO_0
import org.ro.to.SO_1
import org.ro.to.SO_OBJECT_LAYOUT
import org.ro.to.TObject
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@ImplicitReflectionSerializer
class ObjectListTest {

    //FIXME members has a list of named elements @Test
    fun testParse() {
        var jsonStr = SO_0.str
        val ro0 = JSON.parse(TObject.serializer(), jsonStr)
        ro0.addMembersAsProperties()

        jsonStr = SO_1.str
        val ro1 = JSON.parse(TObject.serializer(), jsonStr)
        ro1.addMembersAsProperties()

        jsonStr = SO_OBJECT_LAYOUT.str
        val lyt = JSON.parse(Layout.serializer(), jsonStr)

        val ol = ObjectList()
        ol.initSize(2)

        val oa0 = ObjectAdapter(ro0)
        ol.add(oa0)

        val oa1 = ObjectAdapter(ro1)
        ol.add(oa1)

        ol.setLayout(lyt)
        assertEquals(2, ol.length())

        assertNotNull(ol.getLayout()!!.properties)
    }

}