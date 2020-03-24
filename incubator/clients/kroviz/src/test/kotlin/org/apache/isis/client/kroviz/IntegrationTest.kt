package org.apache.isis.client.kroviz

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.UnstableDefault
import org.apache.isis.client.kroviz.core.aggregator.BaseAggregator
import org.apache.isis.client.kroviz.core.event.EventStore
import org.apache.isis.client.kroviz.core.event.LogEntry
import org.apache.isis.client.kroviz.core.event.ResourceSpecification
import org.apache.isis.client.kroviz.snapshots.Response
import org.apache.isis.client.kroviz.to.Method
import org.apache.isis.client.kroviz.ui.kv.UiManager
import org.apache.isis.client.kroviz.utils.XmlHelper
import org.w3c.xhr.XMLHttpRequest

// subclasses expect a running backend, here SimpleApp localhost:8080/restful*

@UnstableDefault
open class IntegrationTest {

    fun isAppAvailable(): Boolean {
        val user = "sven"
        val pw = "pass"
        val url = "http://${user}:${pw}@localhost:8080/restful/"
        UiManager.login(url, user, pw)
        val credentials: String = UiManager.getCredentials()
        val xhr = XMLHttpRequest();
        xhr.open("GET", url, false, user, pw);
        xhr.setRequestHeader("Authorization", "Basic $credentials")
        xhr.setRequestHeader("Content-Type", "application/json;charset=UTF-8")
        xhr.setRequestHeader("Accept", "application/json")

        try {
            xhr.send(); // there will be a 'pause' here until the response comes.
        } catch (e: Throwable) {
            return false
        }
        val answer = xhr.status.equals(200)
        return answer
    }

    fun mockResponse(response: Response, aggregator: BaseAggregator?): LogEntry {
        val str = response.str
        val subType = if (XmlHelper.isXml(response.str)) {
            "xml"
        } else {
            "json"
        }
        val reSpec = ResourceSpecification(response.url, subType)
        EventStore.start(
                reSpec,
                Method.GET.operation,
                "",
                aggregator)
        val le = EventStore.end(reSpec, str)!!
        org.apache.isis.client.kroviz.handler.ResponseHandler.handle(le)
        wait(100)
        return le
    }

    fun wait(milliseconds: Long) {
        GlobalScope.launch {
            delay(milliseconds)
        }
    }

}
