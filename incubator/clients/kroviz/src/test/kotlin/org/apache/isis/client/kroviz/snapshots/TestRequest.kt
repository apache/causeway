package org.apache.isis.client.kroviz.snapshots

import kotlinx.browser.window
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.await
import org.apache.isis.client.kroviz.IntegrationTest
import org.apache.isis.client.kroviz.to.Link
import org.apache.isis.client.kroviz.ui.core.Constants
import org.w3c.fetch.Response
import kotlin.js.Promise

class TestRequest : IntegrationTest() {

    // see example at @link https://play.kotlinlang.org/hands-on/Building%20Web%20Applications%20with%20React%20and%20Kotlin%20JS/08_Using_an_External_REST_API
    @ExperimentalCoroutinesApi
    suspend fun fetch(link: Link, credentials: String): String {
        val subType = Constants.subTypeJson

        val header: dynamic = object {}
        header["Authorization"] = "Basic $credentials"
        header["Content-Type"] = "application/$subType;charset=UTF-8"
        header["Accept"] = "application/$subType"

        val init: dynamic = object {}
        init["method"] = link.method
        init["header"] = header

        val responsePromise: Promise<Response> = window.fetch(link.href, init)
        val response = responsePromise.await()
        val jsonPromise = response.json()
        val json = jsonPromise.await()
        return json.unsafeCast<String>()
    }

}
