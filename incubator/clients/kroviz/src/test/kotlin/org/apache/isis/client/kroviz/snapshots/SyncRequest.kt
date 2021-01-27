package org.apache.isis.client.kroviz.snapshots

import org.apache.isis.client.kroviz.IntegrationTest
import org.apache.isis.client.kroviz.to.Link
import org.apache.isis.client.kroviz.ui.kv.Constants
import org.w3c.xhr.XMLHttpRequest

class SyncRequest : IntegrationTest() {

    fun invoke(link: Link, credentials: String): String {
        val method = link.method
        val url = link.href

        val xhr = XMLHttpRequest()
        xhr.open(method, url, false)
        xhr.setRequestHeader("Authorization", "Basic $credentials")
        xhr.setRequestHeader("Content-Type", Constants.stdMimeType)
        xhr.setRequestHeader("Accept", Constants.svgMimeType)

        xhr.send()
        while (xhr.readyState != XMLHttpRequest.DONE) {
            wait(100)
            console.log("[SyncRequest.invoke] wait")
        }
        return (xhr.responseText);
    }
}
