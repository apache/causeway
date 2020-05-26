package org.apache.isis.client.kroviz.util

import org.apache.isis.client.kroviz.snapshots.simpleapp1_16_0.PUML_SVG
import org.apache.isis.client.kroviz.utils.ScalableVectorGraphic
import org.w3c.dom.parsing.DOMParser
import kotlin.test.Test
import kotlin.test.assertEquals

class ScalableVectorGraphicTest {

    @Test
    fun testViewBox() {
        //given
        val response = PUML_SVG.str
        val type = "image/svg+xml"
        val p = DOMParser()
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
    fun testScaleDown() {
        //given
        val response = PUML_SVG.str
        val type = "image/svg+xml"
        val p = DOMParser()
        //when
        val svg = ScalableVectorGraphic(response)
        svg.scaleDown()

        // then
        assertEquals((203 * 0.9).toInt(), svg.viewBox.height)
        assertEquals((309 * 0.9).toInt(), svg.viewBox.width)
    }

    @Test
    fun testScaleUp() {
        //given
        val response = PUML_SVG.str
        val type = "image/svg+xml"
        val p = DOMParser()
        //when
        val svg = ScalableVectorGraphic(response)
        svg.scaleUp()

        // then
        assertEquals((203 * 1.1).toInt(), svg.viewBox.height)
        assertEquals((309 * 1.1).toInt(), svg.viewBox.width)
    }

    @Test
    fun testScale2() {
        //given
        val response = PUML_SVG.str
        val type = "image/svg+xml"
        val p = DOMParser()
        //when
        val svg = ScalableVectorGraphic(response)
        svg.scaleUp(2.0)

        //then
        assertEquals(406, svg.viewBox.height)
        assertEquals(618, svg.viewBox.width)
    }

}
