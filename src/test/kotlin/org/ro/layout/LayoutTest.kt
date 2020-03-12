package org.ro.layout

import kotlinx.serialization.UnstableDefault
import org.ro.IntegrationTest
import org.ro.handler.LayoutHandler
import org.ro.snapshots.simpleapp1_16_0.SO_LAYOUT_JSON
import org.ro.to.Links
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@UnstableDefault
class LayoutTest : IntegrationTest() {

    @Test
    fun testDemoTextLayout() {
        //given
        val jsonStr = SO_LAYOUT_JSON.str
        //when
        val layout = LayoutHandler().parse(jsonStr) as Layout
        //val linkList = layout.propertyList
        // then
        assertNotNull(layout)    //1
    }

}
