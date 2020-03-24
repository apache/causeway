package org.apache.isis.client.kroviz.snapshots.simpleapp1_16_0

import kotlinx.serialization.UnstableDefault
import org.apache.isis.client.kroviz.IntegrationTest
import org.apache.isis.client.kroviz.core.aggregator.ActionDispatcher
import org.apache.isis.client.kroviz.core.event.EventStore
import org.apache.isis.client.kroviz.core.event.ResourceSpecification
import org.apache.isis.client.kroviz.to.Link
import org.apache.isis.client.kroviz.to.Method
import kotlin.test.assertEquals

/**
 * This is an integration test that requires SimpleApp running on http://localhost:8080
 *
 * @item Iterate over all Objects defined in package 'urls',
 * @item check if the href 'self' can be invoked and
 * @item compare the response of invoking 'self' with what is hardcoded
 */
@UnstableDefault
class UrlsTest : IntegrationTest() {

    //@Test
    fun testUrls() {
        if (isAppAvailable()) {
            // given
            val urls = mutableMapOf<String, String>()
            urls.put(FR_OBJECT.url, FR_OBJECT.str)
            urls.put(FR_OBJECT_BAZ.url, FR_OBJECT_BAZ.str)
            urls.put(FR_OBJECT_LAYOUT.url, FR_OBJECT_LAYOUT.str)
            urls.put(FR_OBJECT_PROPERTY.url, FR_OBJECT_PROPERTY.str)
            urls.put(FR_PROPERTY_DESCRIPTION.url, FR_PROPERTY_DESCRIPTION.str)
            urls.put(SO_0.url, SO_0.str)
            urls.put(SO_LIST_ALL.url, SO_LIST_ALL.str)
            urls.put(SO_LIST_ALL_INVOKE.url, SO_LIST_ALL_INVOKE.str)
            urls.put(SO_LIST_ALL_OBJECTS.url, SO_LIST_ALL_OBJECTS.str)
            urls.put(SO_MENU.url, SO_MENU.str)
            urls.put(SO_OBJECT_LAYOUT.url, SO_OBJECT_LAYOUT.str)
            urls.put(RESTFUL_SERVICES.url, RESTFUL_SERVICES.str)

            //when
            for (entry in urls) {
                val href = entry.key
                val link = Link(method = Method.GET.operation, href = href)
                ActionDispatcher().invoke(link)
            }

            // then
            wait(1000)
            for (entry in urls) {
                val hrefSpec = ResourceSpecification(entry.key)
                val actual = EventStore.find(hrefSpec)!!.getResponse()
                assertEquals(entry.value, actual)
            }
        }
    }

}
