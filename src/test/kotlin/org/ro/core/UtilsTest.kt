package org.ro.core

import org.ro.to.Argument
import org.ro.to.Link
import org.ro.to.Method
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class UtilsTest {

    @Test
    fun testDecamel() {
        //given
        val word = "OK"
        val expected = "OK"
        //when
        val actual = Utils.deCamel(word)
        //
        assertEquals(expected, actual)
    }

    //@Test //@Test  breaks on Travis #153 ???
    fun testDate() {
        // given
        val rawDate:Any? = "2020-01-25T13:07:05Z"
        //when
        val dateTime = Utils.toDate(rawDate)
        // then
        assertNotNull(dateTime)
        assertEquals(2020, dateTime.getFullYear())
        assertEquals(0, dateTime.getMonth()) // c braintwist strikes again
        assertEquals(14, dateTime.getHours())  // MEZ = GMT + 1
        assertEquals(7, dateTime.getMinutes())  // and again? shouldn't it be 7??
    }

    //@Test  breaks on Travis #152 ? https://travis-ci.com/joerg-rade/kroviz/builds/149958789
    fun test_javaOffsetDateTime() {
        // given
        val rawDate:Any? = "20200125T140705.356+0100"
        val expected:String = "2020-01-25T14:07:05.356+0100"
        //when Then

        val actual = Utils.convertJavaOffsetDateTimeToISO(rawDate as String)
        assertEquals(expected, actual)

        val dateTime = Utils.toDate(actual)
        assertNotNull(dateTime)
        assertEquals(2020, dateTime.getFullYear())
        assertEquals(0, dateTime.getMonth()) // c braintwist strikes again
        assertEquals(25, dateTime.getDate())
        assertEquals(14, dateTime.getHours())  // MEZ = GMT + 1
        assertEquals(7, dateTime.getMinutes())
    }


    // @Test
    fun test_argumentsAsBody() {
        //given
        val href = "http://localhost:8080/restful/services/isisApplib.FixtureScriptsDefault/actions/runFixtureScript/invoke"
        val rel = "urn:org.restfulobjects:rels/invoke;action='runFixtureScript'"
        val type = "application/json;profile='urn:org.restfulobjects:repr-types/object-action'"
        val link = Link(method = Method.POST.operation, href = href, rel = rel, type = type)
//        val arguments = LinkedHashMap<String, String>() //
 //       link.setArgument(arguments)

        // when
        val body = Utils.argumentsAsBody(link)

        // then
        console.log("[UtilsTest.test_argumentsAsBody]")
        console.log(body)
    }

    @Test
    fun testScriptAsBody() {
        //given
        val expected = """"script": {"value": {"href": "http://localhost:8080"}}"""
        val value = "http://localhost:8080"
        val arg = Argument("script", value)
        // when
        val actual = Utils.asBody(arg)
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
        val actual = Utils.asBody(arg)
        // then
        assertEquals(expected, actual)
    }

}
