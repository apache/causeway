package org.apache.isis.client.kroviz.util

import org.apache.isis.client.kroviz.snapshots.simpleapp1_16_0.PUML_SVG
import org.apache.isis.client.kroviz.utils.ScalableVectorGraphic
import kotlin.test.Test
import kotlin.test.assertEquals

class ScalableVectorGraphicTest {

    @Test
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

    @Test
    fun testScaleUp() {
        //given
        val response = PUML_SVG.str
        //when
        val svg = ScalableVectorGraphic(response)
        svg.scaleUp()

        // then
        val w2 = svg.viewBox.width / 2
        val h2 = svg.viewBox.height / 2
        val s = 1.1
        val x = w2 * s - w2
        val y = h2 * s - h2

        assertEquals(x, svg.x)
        assertEquals(y, svg.y)
    }

}
