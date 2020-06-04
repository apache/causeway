package org.apache.isis.client.kroviz.util

import org.apache.isis.client.kroviz.snapshots.simpleapp1_16_0.PUML_SVG
import org.apache.isis.client.kroviz.utils.ScalableVectorGraphic
import kotlin.test.assertEquals

class ScalableVectorGraphicTest {

    fun testViewBox() {
        //given
        val response = PUML_SVG.str
        //when
        val svg = ScalableVectorGraphic(response)
        val viewBox = svg.viewBox
        // then
        assertEquals(0, viewBox.x)
        assertEquals(0, viewBox.y)
        assertEquals(309, viewBox.width)
        assertEquals(203, viewBox.height)
    }

    fun testScaleUp() {
        //given
        val response = PUML_SVG.str
        //when
        val svg = ScalableVectorGraphic(response)
        svg.scaleUp()

        // then
        //TODO extract transform/translate x,y and compare against values calculated
        // assertEquals((203 * 1.1).toInt(), svg.viewBox.height)
        //assertEquals((309 * 1.1).toInt(), svg.viewBox.width)
    }

}
