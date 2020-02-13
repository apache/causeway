package kotlinx.serialization

import kotlinx.serialization.json.Json
import kotlin.test.assertEquals

class TravelTest {

    //@Test
    fun testWithProperties() {
        //given
        val withProperties = """
{
    "route": [
        {
            "location": {
                "latitude": 41.87985,
                "longitude": -87.624936
            },
            "role": "start"
        },
        {
            "location": {
                "latitude": 34.019444,
                "longitude": -118.490278
            },
            "role": "end"
        }
    ]
}
 """
        //when
        val travel = Json.parse(Travel.serializer(), withProperties)
        val routeList = travel.route
        val route = routeList[0]
        val locationList = route.location
        // then
        assertEquals(2, locationList.size)
    }

    //@Test
    fun testWithOutProperties() {
        //given
        val withOutProperties = """
{
    "route": [
        {
            "location": {
                "latitude": 41.87985,
                "longitude": -87.624936
            }
        },
        {
            "location": {
                "latitude": 34.019444,
                "longitude": -118.490278
            }
        }
    ]
}
"""
        //when
        val travel = Json.parse(Travel.serializer(), withOutProperties)
        val routeList = travel.route
        val route = routeList[0]
        val locationList = route.location
        // then
        assertEquals(2, locationList.size)
    }

}
