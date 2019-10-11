package org.ro

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.UnstableDefault
import org.ro.core.Session
import org.ro.core.aggregator.IAggregator
import org.ro.core.event.EventStore
import org.ro.core.event.LogEntry
import org.ro.handler.ResponseHandler
import org.ro.to.Method
import org.ro.urls.Response
import org.w3c.xhr.XMLHttpRequest

// subclasses expect a running backend, here SimpleApp localhost:8080/restful*

@UnstableDefault
open class IntegrationTest {

    fun isSimpleAppAvailable(): Boolean {
        val url = "http://sven:pass@localhost:8080/restful/"
        val user = "sven"
        val pw = "pass"
        Session.login(url, user, pw)
        val credentials: String = Session.getCredentials()
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

    fun mockResponse(response: Response, aggregator: IAggregator?): LogEntry {
        val str = response.str
        val url = response.url
        val method = Method.GET.operation
        EventStore.start(url, method, "", aggregator)
        val le = EventStore.end(url, str)
        ResponseHandler.handle(le!!)
        wait(100)
        return le
    }

    fun wait(milliseconds: Long) {
        GlobalScope.launch {
            delay(milliseconds)
            console.log("[TestUtil.wait] $milliseconds")
        }
    }

}
