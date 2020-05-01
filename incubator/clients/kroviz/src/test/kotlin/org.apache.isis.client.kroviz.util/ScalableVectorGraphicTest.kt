package org.apache.isis.client.kroviz.util

import org.apache.isis.client.kroviz.snapshots.simpleapp1_16_0.PUML_SVG
import org.apache.isis.client.kroviz.utils.ScalableVectorGraphic
import org.w3c.dom.parsing.DOMParser
import kotlin.test.Test
import kotlin.test.assertEquals

class ScalableVectorGraphicTest {

    @Test
    fun testScaleDown() {
        //given
        val response = PUML_SVG.str
        val type = "image/svg+xml"
        val p = DOMParser()
        //when
        val doc = p.parseFromString(response, type)
        val svg = ScalableVectorGraphic(doc)
        svg.scaleDown()

        // then
        assertEquals((203 * 0.9).toInt(), svg.getHeight())
        assertEquals((309 * 0.9).toInt(), svg.getWidth())
    }

    @Test
    fun testScaleUp() {
        //given
        val response = PUML_SVG.str
        val type = "image/svg+xml"
        val p = DOMParser()
        //when
        val doc = p.parseFromString(response, type)
        val svg = ScalableVectorGraphic(doc)
        svg.scaleUp()

        // then
        assertEquals((203 * 1.1).toInt(), svg.getHeight())
        assertEquals((309 * 1.1).toInt(), svg.getWidth())
    }

    @Test
    fun testScale2() {
        //given
        val response = PUML_SVG.str
        val type = "image/svg+xml"
        val p = DOMParser()
        //when
        val doc = p.parseFromString(response, type)
        val svg = ScalableVectorGraphic(doc)
        svg.scaleUp(2.0)

        //then
        assertEquals(406, svg.getHeight())
        assertEquals(618, svg.getWidth())
    }

}
