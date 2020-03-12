package org.ro.core.model

import kotlinx.serialization.UnstableDefault
import org.ro.handler.LayoutHandler
import org.ro.handler.LayoutXmlHandler
import org.ro.handler.TObjectHandler
import org.ro.layout.Layout
import org.ro.snapshots.simpleapp1_16_0.*
import org.ro.to.TObject
import org.ro.to.bs3.Grid
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@UnstableDefault
class DisplayListTest {

    @Test
    fun testConfiguration() {
        val ro0 = TObjectHandler().parse(CFG_1.str) as TObject
        val lt = LayoutHandler().parse(CFG_LAYOUT_JSON.str) as Layout
       // val grd = LayoutXmlHandler().parse(CFG_LAYOUT_XML.str) as Grid

        val dl = DisplayList("test")
        dl.addData(ro0)

        dl.addLayout(lt)
        assertEquals(1, dl.data.size) //1

        val properties = dl.propertyList
        assertEquals("key", properties[0].id) // 2
        assertEquals("value", properties[1].id)   // 3
    }

    //@Test
    fun testFixtureResult() {
        val ro0 = TObjectHandler().parse(FR_OBJECT.str) as TObject
        val lt = LayoutXmlHandler().parse(FR_OBJECT_LAYOUT.str) as Layout

        val dl = DisplayList("test")
        dl.addData(ro0)

        dl.addLayout(lt)
        assertEquals(1, dl.data.size)

        assertNotNull(dl.layout)
        val properties = dl.propertyList
        assertNotNull(properties)
        //Sequence in FR_OBJECT differs from sequence in FR_OBJECT_LAYOUT
        // FR_OBJECT: fixtureScriptClassName, key, object, className
        // FR_OBJECT_LAYOUT: className, fixtureScriptClassName, key, object
        assertEquals("fixtureScriptClassName", properties[1].id)
        assertEquals("key", properties[2].id)
        assertEquals("object", properties[3].id)
        assertEquals("className", properties[0].id)
    }

    @Test
    fun testSimpleObject() {
        val ro0 = TObjectHandler().parse(SO_0.str) as TObject
        val ro1 = TObjectHandler().parse(SO_1.str) as TObject
        val lt = LayoutHandler().parse(SO_OBJECT_LAYOUT.str) as Layout

        val dl = DisplayList("test")
        dl.addData(ro0)
        dl.addData(ro1)
        dl.addLayout(lt)

        assertEquals(2, dl.data.size) //1

        val properties = dl.propertyList
        assertEquals(2, properties.size) //2
        //FIXME
        assertEquals("name", properties[0].id) //3
        assertEquals("notes", properties[1].id)  //4
    }

    //TODO add test that ensures sequence is preserved in members / getMembers
    //  map.forEach { (key, value) -> println("$key = $value") }
    // val numbersMap = mapOf("one" to 1, "two" to 2, "three" to 3)
    //println(numbersMap + Pair("four", 4))
    //println(numbersMap + Pair("one", 10))
    //println(numbersMap + mapOf("five" to 5, "one" to 11))

}
