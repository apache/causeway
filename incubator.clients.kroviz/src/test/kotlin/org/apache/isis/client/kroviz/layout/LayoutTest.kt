package org.apache.isis.client.kroviz.layout

import kotlinx.serialization.UnstableDefault
import org.apache.isis.client.kroviz.IntegrationTest
import org.apache.isis.client.kroviz.handler.LayoutHandler
import org.apache.isis.client.kroviz.snapshots.simpleapp1_16_0.SO_LAYOUT_JSON
import kotlin.test.Test
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
