package org.ro.to

import kotlin.test.Test
import kotlin.test.assertEquals

class ArgumentTest {

    @Test
    fun testScriptAsBody() {
        //given
        val expected = """"script": {"value": {"href": "http://localhost:8080"}}"""
        val value = "http://localhost:8080"
        val arg = Argument("script", value)
        // when
        val actual = arg.asBody()
        // then
        assertEquals(expected, actual)
    }

    @Test
    fun testParametersAsBody() {
        //given
        val expected = """"parameters": {"value": ""}"""
        val value = ""
        val arg = Argument("parameters", value)
        // when
        val actual = arg.asBody()
        // then
        assertEquals(expected, actual)
    }
}