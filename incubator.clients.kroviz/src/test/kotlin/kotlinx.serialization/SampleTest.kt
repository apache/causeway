package kotlinx.serialization

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class SampleTest {

    @UnstableDefault
    @Test
    fun testInnerObjects() {
        //given
        val jsonStr = """
{
    "sample": [
        {
            "inner": {
                "value": "value1"
            },
            "inner": {
                "value": "value2"
            }
        }
    ]
}
 """
        //when
        val root = Json.parse(Sample.serializer(), jsonStr)
        // then
        val outerList = root.sample
        assertEquals(1, outerList.size)

        val outer = outerList[0]
        val innerList = outer.inner // expected is a list of Inner
        // then
        //assertEquals(2, innerList.size)
    }

}
